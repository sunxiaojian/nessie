/*
 * Copyright (C) 2024 Dremio
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

import java.time.Duration;
import java.util.Optional;
import org.projectnessie.catalog.files.adls.AdlsFileSystemOptions;

public interface CatalogAdlsFileSystemOptions extends AdlsFileSystemOptions {
  @Override
  Optional<String> accountName();

  @Override
  Optional<String> accountKey();

  @Override
  Optional<String> sasToken();

  @Override
  Optional<String> endpoint();

  @Override
  Optional<String> externalEndpoint();

  @Override
  Optional<AdlsRetryStrategy> retryPolicy();

  @Override
  Optional<Integer> maxRetries();

  @Override
  Optional<Duration> tryTimeout();

  @Override
  Optional<Duration> retryDelay();

  @Override
  Optional<Duration> maxRetryDelay();
}
