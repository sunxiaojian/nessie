/*
 * Copyright (C) 2022 Dremio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.projectnessie.quarkus.providers.storage;

import static org.projectnessie.versioned.storage.common.logic.Logics.repositoryLogic;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.projectnessie.quarkus.config.QuarkusStoreConfig;
import org.projectnessie.quarkus.config.VersionStoreConfig;
import org.projectnessie.quarkus.config.VersionStoreConfig.VersionStoreType;
import org.projectnessie.quarkus.providers.NotObserved;
import org.projectnessie.quarkus.providers.UninitializedRepository;
import org.projectnessie.quarkus.providers.versionstore.StoreType.Literal;
import org.projectnessie.services.config.ServerConfig;
import org.projectnessie.versioned.storage.cache.CacheBackend;
import org.projectnessie.versioned.storage.cache.CacheConfig;
import org.projectnessie.versioned.storage.cache.CacheSizing;
import org.projectnessie.versioned.storage.cache.PersistCaches;
import org.projectnessie.versioned.storage.common.persist.Backend;
import org.projectnessie.versioned.storage.common.persist.Persist;
import org.projectnessie.versioned.storage.common.persist.PersistFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PersistProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(PersistProvider.class);

  private final Instance<BackendBuilder> backendBuilder;
  private final Instance<Backend> backend;
  private final VersionStoreConfig versionStoreConfig;
  private final ServerConfig serverConfig;
  private final QuarkusStoreConfig storeConfig;

  @Inject
  public PersistProvider(
      @Any Instance<BackendBuilder> backendBuilder,
      @Any Instance<Backend> backend,
      VersionStoreConfig versionStoreConfig,
      QuarkusStoreConfig storeConfig,
      ServerConfig serverConfig) {
    this.backendBuilder = backendBuilder;
    this.backend = backend;
    this.versionStoreConfig = versionStoreConfig;
    this.storeConfig = storeConfig;
    this.serverConfig = serverConfig;
  }

  @Produces
  @Singleton
  public Backend produceBackend() {
    VersionStoreType versionStoreType = versionStoreConfig.getVersionStoreType();

    if (backendBuilder.isUnsatisfied()) {
      throw new IllegalStateException("No Quarkus backend implementation for " + versionStoreType);
    }

    return backendBuilder.select(new Literal(versionStoreType)).get().buildBackend();
  }

  public void closeBackend(@Disposes Backend backend) throws Exception {
    if (backend != null) {
      String info = backend.configInfo();
      if (!info.isEmpty()) {
        info = " (" + info + ")";
      }
      LOGGER.info("Stopping storage for {}{}", versionStoreConfig.getVersionStoreType(), info);
      backend.close();
    }
  }

  @Produces
  @Singleton
  @Default
  public Persist produceWithInitializedRepository(@UninitializedRepository Persist persist) {
    repositoryLogic(persist).initialize(serverConfig.getDefaultBranch());
    return persist;
  }

  /**
   * Eagerly initialize the not-observed {@link Persist} instance.
   *
   * <p>{@link io.quarkus.runtime.Startup @Startup} mustn't be used with {@link Produces @Produces},
   * instead an event-observer for {@link StartupEvent} shall be used, taking the produced bean as
   * an argument.
   */
  public void eagerPersistInitialization(
      @Observes StartupEvent event, @NotObserved Persist persist) {
    LOGGER.debug("Eager initialization of persist implementation '{}'", persist.name());
  }

  @Produces
  @Singleton
  @NotObserved
  public Persist producePersist(MeterRegistry meterRegistry) {
    VersionStoreType versionStoreType = versionStoreConfig.getVersionStoreType();

    if (backend.isUnsatisfied()) {
      throw new IllegalStateException("No Quarkus backend for " + versionStoreType);
    }

    Backend b = backend.get();
    b.setupSchema();

    LOGGER.info("Creating/opening version store {} ...", versionStoreType);

    PersistFactory persistFactory = b.createFactory();
    Persist persist = persistFactory.newPersist(storeConfig);

    String info = b.configInfo();
    if (!info.isEmpty()) {
      info = " (" + info + ")";
    }

    CacheSizing cacheSizing =
        CacheSizing.builder()
            .fixedSizeInMB(storeConfig.cacheCapacityMB())
            .fractionMinSizeMb(storeConfig.cacheCapacityFractionMinSizeMb())
            .fractionOfMaxHeapSize(storeConfig.cacheCapacityFractionOfHeap())
            .heapSizeAdjustmentMB(storeConfig.cacheCapacityFractionAdjustMB())
            .build();
    int effectiveCacheSizeMB = cacheSizing.effectiveSizeInMB();

    String cacheInfo;
    if (effectiveCacheSizeMB > 0) {
      CacheConfig.Builder cacheConfig =
          CacheConfig.builder().capacityMb(effectiveCacheSizeMB).meterRegistry(meterRegistry);

      storeConfig
          .referenceCacheTtl()
          .ifPresent(
              refTtl -> {
                LOGGER.warn(
                    "Reference caching is an experimental feature but enabled with a TTL of {}",
                    refTtl);
                cacheConfig.referenceTtl(refTtl);
              });
      storeConfig.referenceCacheNegativeTtl().ifPresent(cacheConfig::referenceNegativeTtl);

      CacheBackend cacheBackend = PersistCaches.newBackend(cacheConfig.build());
      persist = cacheBackend.wrap(persist);
      cacheInfo = "with " + effectiveCacheSizeMB + " MB objects cache";
    } else {
      cacheInfo = "without objects cache";
    }

    LOGGER.info("Using {} version store{}, {}", versionStoreType, info, cacheInfo);

    return persist;
  }
}
