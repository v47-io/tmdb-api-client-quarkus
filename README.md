# TMDb API Client (Quarkus Extension)

![GitHub Workflow Status](https://img.shields.io/github/workflow/status/v47-io/tmdb-api-client-quarkus/Build)
[![Maven Central](https://img.shields.io/maven-central/v/io.v47.tmdb-api-client/quarkus)](https://search.maven.org/artifact/io.v47.tmdb-api-client/quarkus)
![GitHub](https://img.shields.io/github/license/v47-io/tmdb-api-client-quarkus)

This is the extension that provides proper Quarkus support for [TMDb API Client][tmdb-api-client].

[tmdb-api-client]: https://github.com/v47-io/tmdb-api-client

## Usage

Add the following dependency to your project:

```groovy
// Gradle
implementation 'io.v47.tmdb-api-client:quarkus:1.4.0'
```

```kotlin
// Gradle Kotlin DSL
implementation("io.v47.tmdb-api-client:quarkus:1.4.0")
```

```xml
<!--Maven-->
<dependencies>
    <dependency>
        <groupId>io.v47.tmdb-api-client</groupId>
        <artifactId>quarkus</artifactId>
        <version>1.4.0</version>
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

### Using a custom `TmdbApiKeyProvider`

By default, the extension will use its own implementation that uses the configuration property
mentioned above. If you want to control how the API Key is obtained, e.g. dynamically during
runtime, you can create your own `TmdbApiKeyProvider` implementation.

__IMPORTANT:__ You must still provide a value for the configuration property `tmdb.client.api-key`.
However, since you are providing your own implementation of `TmdbApiKeyProvider` it may be a value
like `dummy` or `not a valid API key`.

#### Example implementation

```java
package myPackage;

import io.quarkus.arc.Priority;
import io.v47.tmdb.api.key.TmdbApiKeyProvider;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

@Alternative
@Priority(1)
@ApplicationScoped
public class MyApiKeyProvider implements TmdbApiKeyProvider {
    @NotNull
    @Override
    public String getApiKey() {
        return "my-own-api-key";
    }
}
```

Quarkus will then use an instance of `MyApiKeyProvider` when creating the default `TmdbClient` bean.

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
