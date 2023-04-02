# Evaluation lab - Apache Kafka

## Group number: 21

## Group members

- Francesco Aristei 10804304
- Dario Mazzola 10650009
- Giampiero Repole 10543357

## Problem addressed

One instance of the Producer class publishes messages into the topic “inputTopic”.
– The producer is idempotent.
– Message keys are String.
– Message values are Integer.
You may set the number of partitions for “inputTopic” using the TopicManager class.
– Indicate in the README.md file the minimum and maximum number of partitions allowed.
Consumers take in input at least one argument.
– The first one is the consumer group.
– You may add any other argument you need.

### Request 1

Implement a FilterForwarder
– It consumes messages from “InputTopic”.
– It forwards messages to “OutputTopic”.
– It forwards only messages with a value greater than threshold (which is an attribute of the class)
– It provides exactly once semantics.
All messages that overcome the threshold need to be forwarded to once and only once.

### Request 2
Implement a AverageConsumer
– It computes the average value across all keys (i.e., the sum of the last value received for all keys divided by the number of keys)
– It prints the average value every time it changes
– It does NOT provide guarantees in the case of failures
Input messages may be lost or considered more than once in the case of failures.
The average value may be temporarily incorrect in the case of failures.


## Solution to request 1

- Number of partitions allowed for TopicA (min = 1, max = K)
Since numKeys value in the producer is set to 100, it must be K <= 100. 
Indeed, if the number of partitions is larger than the number of keys those partition will never be used, 
because Kafka ensures that messages with the same key will be stored in the same partition. 

- Number of consumers allowed (min = 1, max = M)
  with M <= K
    - Consumer 1: "Group1"
    - Consumer 2: "Group1"
    - ...
    - Consumer M: "Group1"

It is possible to have an arbitrary number of different consumer groups and at most M consumers for each consumer group because
the useful number of consumers in the same group is less or equal than the number of partitions. 

## Solution to request 2

- Number of partitions allowed for TopicA (min = 1, max = K)
Since numKeys value in the producer is set to 100, it must be K <= 100. Indeed, if the number of partitions 
is larger than the number of keys those partition will never be used, because Kafka ensures that messages 
with the same key will be stored in the same partition.

- Number of consumers allowed (min = 1, max = M)
  with M <= K
    - Consumer 1: "Group1"
    - Consumer 2: "Group1"
    - ...
    - Consumer M: "Group1"

It is possible to have an arbitrary number of different consumer groups and at most M consumers for each consumer group because 
the useful number of consumers in the same group is less or equal to the number of partitions.
