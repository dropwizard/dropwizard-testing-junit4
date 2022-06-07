# Dropwizard Testing Support for JUnit 4.x

[![Build](https://github.com/dropwizard/dropwizard-testing-junit4/workflows/Build/badge.svg)](https://github.com/dropwizard/dropwizard-testing-junit4/actions?query=workflow%3ABuild)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dropwizard_dropwizard-testing-junit4&metric=alert_status)](https://sonarcloud.io/dashboard?id=dropwizard_dropwizard-testing-junit4)
[![Maven Central](https://img.shields.io/maven-central/v/io.dropwizard.modules/dropwizard-testing-junit4.svg)](http://mvnrepository.com/artifact/io.dropwizard.modules/dropwizard-testing-junit4)

The `dropwizard-testing-junit4` module provides you with support for testing [Dropwizard] 3.0.x or later applications with [JUnit 4.x].

### Deprecation note

It is recommended that new projects use the `dropwizard-testing` module with support for [JUnit 5.x].

Please refer to the JUnit 5 User Guide: [Migrating from JUnit 4] for details about how to migrate to [JUnit 5.x].

[JUnit 4.x]: https://junit.org/junit4/
[JUnit 5.x]: https://junit.org/junit5/
[Dropwizard]: http://dropwizard.io/
[Migrating from JUnit 4]: https://junit.org/junit5/docs/5.8.0/user-guide/#migrating-from-junit4

## Maven Artifacts

This project is available on Maven Central. To add it to your project you can add the following dependencies to your
`pom.xml`:

    <dependency>
      <groupId>io.dropwizard.modules</groupId>
      <artifactId>dropwizard-testing-junit4</artifactId>
      <version>${dropwizard.version}</version>
    </dependency>

## Usage

Adding `DropwizardAppRule` to your JUnit4 test class will start the app
prior to any tests running and stop it again when they've completed
(roughly equivalent to having used `@BeforeClass` and `@AfterClass`).
`DropwizardAppRule` also exposes the app's `Configuration`,
`Environment` and the app object itself so that these can be queried by
the tests.

If you don't want to use the `dropwizard-client` module or find it
excessive for testing, you can get access to a Jersey HTTP client by
calling the <span class="title-ref">client</span> method on the rule.
The returned client is managed by the rule and can be reused across
tests.

```java
public class LoginAcceptanceTest {
    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE =
        new DropwizardAppRule<>(MyApp.class, ResourceHelpers.resourceFilePath("my-app-config.yaml"));

    @Test
    public void loginHandlerRedirectsAfterPost() {
        Client client = RULE.client();
        Response response = client.target(
                String.format("http://localhost:%d/login", RULE.getLocalPort()))
                .request()
                .post(Entity.json(loginForm()));
        assertThat(response.getStatus()).isEqualTo(302);
    }
}
```

**Warning**: 

Resource classes are used by multiple threads concurrently. 
In general, we recommend that resources be stateless/immutable, but it's important to keep the context in mind.

## Support

Please file bug reports and feature requests in [GitHub issues](https://github.com/dropwizard/dropwizard-testing-junit4/issues).


## License

Copyright (c) 2022 Dropwizard Team

This library is licensed under the Apache License, Version 2.0.

See http://www.apache.org/licenses/LICENSE-2.0.html or the LICENSE file in this repository for the full license text.
