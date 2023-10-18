/*
 * Copyright (C) 2023 Dremio
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
package org.projectnessie.quarkus.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

@StaticInitSafe
@ConfigMapping(prefix = "nessie.version.store.persist.bigtable")
public interface QuarkusBigTableConfig {

  @WithDefault("nessie")
  String instanceId();

  Optional<String> appProfileId();

  Optional<String> quotaProjectId();

  Optional<String> endpoint();

  Optional<String> mtlsEndpoint();

  Optional<String> emulatorHost();

  Optional<String> tablePrefix();

  Map<String, String> jwtAudienceMapping();

  Optional<Duration> maxRetryDelay();

  OptionalInt maxAttempts();

  Optional<Duration> initialRpcTimeout();

  Optional<Duration> totalTimeout();

  Optional<Duration> initialRetryDelay();

  OptionalInt minChannelCount();

  OptionalInt maxChannelCount();

  OptionalInt initialChannelCount();

  OptionalInt minRpcsPerChannel();

  OptionalInt maxRpcsPerChannel();

  @WithDefault("false")
  boolean noTableAdminClient();

  @WithDefault("8086")
  int emulatorPort();

  @WithDefault("true")
  boolean enableTelemetry();
}
