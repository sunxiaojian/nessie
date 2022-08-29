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

plugins {
  `java-library`
  jacoco
  `maven-publish`
  signing
  `nessie-conventions`
}

extra["maven.name"] = "Nessie - Versioned - Persist - DynamoDB"

dependencies {
  implementation(platform(rootProject))
  compileOnly(platform(project(":nessie-deps-build-only")))
  annotationProcessor(platform(project(":nessie-deps-build-only")))
  implementation(platform("software.amazon.awssdk:bom:${dependencyVersion("versionAwssdk")}"))

  implementation(project(":nessie-versioned-persist-adapter"))
  implementation(project(":nessie-versioned-persist-non-transactional"))
  implementation(project(":nessie-versioned-persist-serialize"))
  implementation(project(":nessie-versioned-spi"))
  compileOnly("org.immutables:value-annotations")
  annotationProcessor("org.immutables:value-processor")
  implementation("com.google.code.findbugs:jsr305")
  implementation("com.google.guava:guava")
  implementation("software.amazon.awssdk:dynamodb") {
    exclude("software.amazon.awssdk", "apache-client")
  }
  implementation("software.amazon.awssdk:netty-nio-client")
  implementation("software.amazon.awssdk:url-connection-client")

  testImplementation(platform(project(":nessie-deps-testing")))
  testImplementation(platform("org.junit:junit-bom"))

  testImplementation(project(":nessie-versioned-tests"))
  testImplementation(project(":nessie-versioned-persist-testextension"))
  testImplementation(project(":nessie-versioned-persist-tests"))
  testImplementation(project(":nessie-versioned-persist-non-transactional-test"))
  testImplementation(project(":nessie-versioned-persist-dynamodb-test"))

  testImplementation("org.assertj:assertj-core")
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.named<Test>("test") { maxParallelForks = Runtime.getRuntime().availableProcessors() }
