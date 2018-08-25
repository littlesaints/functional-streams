# functional-streams

Smart functions for stream processing, fault-tolerance and Java Streams integration of non-streaming APIs and language constructs.

[API documentation](https://littlesaints.gitlab.io/functional-streams/api/)

### Functions for stream processing

#### - ForkJoin
This class facilitates combining multiple streams of input.

It can be used in the following ways:
 
- Fork (create) multiple streams from a single stream.
  The application can configure multiple forks, with or without predicates.
  If a predicate is configured, the forked stream will receive the input only when the predicate is satisfied i.e. return 'true'.

- Join multiple streams in a single stream.
  The application can configure multiple forks, ideally with a predicate / filter which fork met, would push the input in it's corresponding stream.

- Combine 'm' input streams into 'n' output streams.
 
[more details and example](https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/streams/ForkJoin.html)

#### - Trial
Functional way to re-attempt failures. It supports configuring a re-attempt strategy and can perform a phased-backoff on continuous failures. 

It does all the complex state management and tracking of failures, so the application can focus on it's business logic.

[more details and example](https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/trial/Trial.html)

#### - Aggregator
It can be used to aggregate or batch inputs based on a predicate.

A use-case can be of creating an archive, whose size is closest possible to a threshold.

[more details and example](https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/streams/Aggregator.html)

#### - StreamSource
Function to generate a Stream from non-compatible sources. 

It turns the push based approach of retrieving inputs to a Streams like pull based approach. 
This means the application can wire the logic to pull inputs for a Stream but it'll be called when the Stream is executed i.e. upon execution of the terminal operation of the Java Stream.

[more details and example](https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/streams/StreamSource.html)

### Functional replacement for control statements with Java Streams

#### - If
Functional replacement of an 'if-else' construct. Integrates with Java Streams.

[more details and example](https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/streams/If.html)

#### - Switch
Functional replacement of an 'switch-case' construct. Integrates with Java Streams.

[more details and example](https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/streams/Switch.html)

### Functional replacement for exception handling with Java Streams

#### - Try
Functional way of exceptional handling in Java Streams. It supports onSuccess and onFailure actions on any
operation that can cause an exception. 

It's a good fit for handling external api calls.

[more details and example](https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/streams/Try.html)

### General purpose functions

#### - moduloForPowerOfTwo
A function to calculate much faster modulo operation with any number that is a power of 2, using a '&' instead of the '%' operator.

It's useful when doing any conditional routing for data processing, whether relative order needs to be maintained.

[more details and javadoc](https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/maths/Mathematician.html#field.summary)

#### - isPowerOfTwo
A function to check whether a number is a power of 2. This is useful to know if a faster modulo operation can be performed. 

[more details and javadoc](https://littlesaints.gitlab.io/functional-streams/api/com/littlesaints/protean/functions/maths/Mathematician.html#field.summary)

### License
GNU GENERAL PUBLIC LICENSE Version 3

Copyright (C) 2018  Varun Anand
