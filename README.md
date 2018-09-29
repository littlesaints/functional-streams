# functional-streams

Smart functions for stream processing, fault-tolerance and Java Streams integration of non-streaming APIs and language constructs.

[![javadoc][api_badge]][api] [![maven-central][maven-central_badge]][maven-central] [![build][build_badge]][build] [![coverage][coverage_badge]][coverage] [![tests][tests_badge]][tests] [![license][license_badge]][lgplv3]

## Contents
- Functions integrated with Java Streams
    - [Stream processing extensions](#stream-processing-extensions)
    - [Functional replacement for control statements](#functional-replacement-for-control-statements)
    - [Functional replacement for exception handling](#functional-replacement-for-exception-handling)
    - [General purpose functions](#general-purpose-functions)
- [Usage](#usage)
- [License](#license)

### Stream processing extensions

#### - ForkJoin
This class facilitates combining multiple streams of same input type.

![forkjoin.png](docs/images/forkjoin.png)

It can be used in the following ways:
 
- Fork (create) multiple streams from a single stream. i.e. to implement a workflow or similar.
  The application can configure multiple forks, with or without predicates.
  If a predicate is configured, the forked stream will receive the input only when the predicate is satisfied i.e. return 'true'.

- Join multiple streams in a single stream.
  The application can configure multiple forks, ideally with a predicate / filter which fork met, would push the input in it's corresponding stream.

- Combine 'm' input streams into 'n' output streams.
 
[more details and example][api-forkjoin]

#### - Trial
Functional way to re-attempt failures. It supports configuring a re-attempt strategy and can perform a phased-backoff on continuous failures. 

It does all the complex state management and tracking of failures, so the application can focus on it's business logic.

[more details and example][api-trial]

#### - Aggregator
It can be used to aggregate or batch input streams of a type, based on a predicate.

A use-case can be of creating an archive, whose size is closest possible to a threshold.

![aggregator.png](docs/images/aggregator.png)

[more details and example][api-aggregator]

#### - StreamSource
Function to generate a Stream from non-compatible sources. 

It turns the push based approach of retrieving inputs to a Streams like pull based approach. 
This means the application can wire the logic to pull inputs for a Stream but it'll be called when the Stream is executed i.e. upon execution of the terminal operation of the Java Stream.

[more details and example][api-streamsource]

### Functional replacement for control statements

#### - If
Functional replacement of an 'if-else' construct. Integrates with Java Streams.

[more details and example][api-if]

#### - Switch
Functional replacement of an 'switch-case' construct. Integrates with Java Streams.

[more details and example][api-switch]

### Functional replacement for exception handling

#### - Try
Functional way of exceptional handling in Java Streams. It supports onSuccess and onFailure actions on any
operation that can cause an exception. 

It's a good fit for handling external api calls.

[more details and example][api-try]

### General purpose functions

#### - moduloForPowerOfTwo
A function to calculate much faster modulo operation with any number that is a power of 2, using a '&' instead of the '%' operator.

It's useful when doing any conditional routing for data processing, whether relative order needs to be maintained.

[more details and javadoc][api-mathematician-fields]

#### - isPowerOfTwo
A function to check whether a number is a power of 2. This is useful to know if a faster modulo operation can be performed. 

[more details and javadoc][api-mathematician-fields]

### Usage
Download the [latest version][latest-release] from [Maven Central][releases] or depend via Gradle:

```gradle
compile 'io.littlesaints.gitlab:functional-streams:1.0.0'
```

Snapshot versions are available in [Sonatype's snapshots repository][snapshots].

## Maintainer
[@varunanandrajput](https://gitlab.com/varunanandrajput)

### License
[GNU Lesser General Public License v3](lgplv3)

Copyright (C) 2018 Varun Anand

[latest-release]: https://mvnrepository.com/artifact/io.gitlab.littlesaints/functional-streams/latest
[releases]: https://mvnrepository.com/artifact/io.gitlab.littlesaints/functional-streams
[snapshots]: https://oss.sonatype.org/content/repositories/snapshots/io/gitlab/littlesaints/functional-streams
[api_badge]: https://img.shields.io/badge/docs-API-orange.svg                                                 
[api]: https://littlesaints.gitlab.io/functional-streams/api
[api-forkjoin]: https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/streams/ForkJoin.html
[api-aggregator]: https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/streams/Aggregator.html
[api-streamsource]: https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/streams/StreamSource.html
[api-trial]: https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/trial/Trial.html
[api-if]: https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/streams/If.html
[api-switch]: https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/streams/Switch.html
[api-try]: https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/streams/Try.html
[api-mathematician-fields]: https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/maths/Mathematician.html#field.summary
[maven-central_badge]: https://maven-badges.herokuapp.com/maven-central/io.littlesaints.gitlab/functional-streams/badge.svg
[maven-central]: https://mvnrepository.com/artifact/io.gitlab.littlesaints/functional-streams
[build_badge]: https://gitlab.com/littlesaints/functional-streams/badges/master/build.svg
[build]: https://gitlab.com/littlesaints/functional-streams/pipelines
[coverage_badge]: https://gitlab.com/littlesaints/functional-streams/badges/master/coverage.svg?job=build
[coverage]: https://littlesaints.gitlab.io/functional-streams/coverage
[tests_badge]: https://img.shields.io/badge/report-tests-blue.svg
[tests]: https://littlesaints.gitlab.io/functional-streams/test
[license_badge]: https://img.shields.io/badge/license-LGPLv3-blue.svg
[lgplv3]: https://www.gnu.org/licenses/lgpl-3.0.en.html
