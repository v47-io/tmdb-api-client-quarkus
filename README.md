# TMDb API Client (Quarkus Extension)

![GitHub Workflow Status](https://img.shields.io/github/workflow/status/v47-io/tmdb-api-client-quarkus/Build)
![Maven Central](https://img.shields.io/maven-central/v/io.v47.tmdb-api-client/quarkus)
![GitHub](https://img.shields.io/github/license/v47-io/tmdb-api-client-quarkus)

This is the extension that provides proper Quarkus support for [TMDb API Client][tmdb-api-client].

[tmdb-api-client]: https://github.com/v47-io/tmdb-api-client

## Usage

Add the following dependency to your project:

```groovy
// Gradle
implementation 'io.v47.tmdb-api-client:quarkus:1.0.0-SNAPSHOT'
```

```kotlin
// Gradle Kotlin DSL
implementation("io.v47.tmdb-api-client:quarkus:1.0.0-SNAPSHOT")
```

```xml
<!--Maven-->
<dependencies>
    <dependency>
        <groupId>io.v47.tmdb-api-client</groupId>
        <artifactId>quarkus</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

This will include the `quarkus`, `quarkus-deployment`, `api`, and `core` packages in your project,
so you can get started immediately.

Add the following configuration property to your `application.properties`:

```properties
tmdb.client.api-key=${API_KEY}
```

Then you can simply inject an instance of `TmdbClient` into your services and controllers:

```java
package myPackage;

import io.v47.tmdb.TmdbClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MyService {
    private final TmdbClient tmdbClient;

    @Inject
    public MyService(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }
}
```

## Features

This library makes it possible to use the `TMDb API Client` in Quarkus and also does all the
required legwork to make it native compatible, so you can safely use it in your native Quarkus
applications.

The Quarkus extension provides a default instance of `TmdbClient`, but if you want to create your
own, e.g. because you want to supply a different API-Key at runtime, you can simply inject an 
instance of `HttpClientFactory` and create your own `TmdbClient` instance.

## Documentation

Please refer to the documentation of `TMDb API Client` [here][tmdb-api-client-docs].

[tmdb-api-client-docs]: https://v47-io.github.io/tmdb-api-client/

## Building

Requirements:

- JDK 11
- GraalVM (esp. `native-image`) or Docker

For local building and testing you need to configure a TMDb API-Key using the environment variable
`API_KEY`.

Then you can simply run a complete build using these commands:

```shell
# For the runtime and deployment modules
./mvnw install

# To run the native integration tests (most important)
cd integration-tests
../mvnw verify -P native-image
```
