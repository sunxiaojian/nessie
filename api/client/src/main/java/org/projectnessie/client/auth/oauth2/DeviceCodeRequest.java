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
package org.projectnessie.client.auth.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * A device authorization request as defined in <a
 * href="https://tools.ietf.org/html/rfc8628#section-3.1">RFC 8628 Section 3.1</a>.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDeviceCodeRequest.class)
@JsonDeserialize(as = ImmutableDeviceCodeRequest.class)
interface DeviceCodeRequest extends PublicClientRequest {

  @Nullable
  @JsonProperty("scope")
  String getScope();

  static Builder builder() {
    return ImmutableDeviceCodeRequest.builder();
  }

  interface Builder extends PublicClientRequest.Builder<DeviceCodeRequest> {
    @CanIgnoreReturnValue
    Builder scope(String scope);

    @SuppressWarnings("ClassEscapesDefinedScope")
    DeviceCodeRequest build();
  }
}
