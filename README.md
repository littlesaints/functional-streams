# functional-streams

### Functions for stream processing

#### - Fork
Creates multiple streams from a single stream of inputs. It can be configured for firstMatch
or allMatch modes.

[more details and example](link to javadoc)

#### - Trial
Functional way to re-attempt failures. It supports configuring a re-attempt strategy and can perform a phased-backoff on continuous failures. 

It does all the complex state management and tracking of failures, so the application can focus on it's business logic.

[more details and example](link to javadoc)

#### - StreamHead
Function to generate a Java Stream from non-compatible sources. 

It turns the push based approach of retrieving inputs to a Streams like pull based approach. 
This means the application can wire the logic to pull inputs for a Stream but it'll be called when the Stream is executed i.e. upon execution of the terminal operation of the Java Stream.

[more details and example](link to javadoc)

### Functional replacement for control statements with Java Streams

#### - If
Functional replacement of an 'if-else' construct. Integrates with Java Streams.
[more details and example](link to javadoc)

#### - Switch
Functional replacement of an 'switch-case' construct. Integrates with Java Streams.
[more details and example](link to javadoc)

### Functional replacement for exception handling with Java Streams

#### - Try
Functional way of exceptional handling in Java Streams. It supports onSuccess and onFailure actions on any
operation that can cause an exception. It's a good fit for handling external api calls.

[more details and example](link to javadoc)

### General purpose functions

#### - moduloForPowerOfTwo
A function to calculate much faster modulo operation with any number that is a power of 2, using a '&' instead of the '%' operator.
It's useful when doing any conditional routing for data processing, whether relative order needs to be maintained.

[more details and javadoc](link to javadoc)

#### - isPowerOfTwo
A function to check whether a number is a power of 2. This is useful to know if a faster modulo operation can be performed. 

[more details and javadoc](link to javadoc)