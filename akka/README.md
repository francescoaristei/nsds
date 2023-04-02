# Evaluation lab - Akka

## Group number: 21

## Group members

- Francesco Aristei 10804304
- Dario Mazzola 10650009
- Giampiero Repole 10543357

## Problem to address

You are to create a simple event-based communication system using actors.

The system shall be composed of (at least) five actors:
- Two worker actors matching events against subscriptions.
- A broker actor coordinating the operation of worker(s).
- A subscriber actor that issues subscriptions and receives notifications.
- A publisher actor that generates events.

The system shall use (at least) four types of
messages:
- SubscribeMsg to issue subscriptions.
- PublishMsg to generate events.
- NotificationMsg to notify subscribers of events.
- BatchMsg to change the broker message policy.

The broker splits the matching process between the two worker actors as follows:
- Splitting is based on a partitioning of the key attribute in the Subscribe message.
- Even keys go to one worker, odd keys go to the other.
- When a worker actor receives a Publish message for a topic it is not aware of, it fails.
- Handling the failure must ensure that the set of active subscriptions before the failure is retained.
- You can assume that at most one subscriber exist for a given topic.

Whenever the broker receives a BatchMsg, it looks at the isOn attribute:
- If it is false, the broker shall immediately process every subsequent message it receives.
- If it is true, the broker shall buffer all event messages it receives since that time, and process them in a batch as soon as it receives another BatchMsg with isOn set to false.

You may begin solving this exercise without this feature, then you add it later on.

## Solution adopted

### Description of message flows

When the system starts, it sends to all the actors a ConfigMsg in order to locate the actors which they have to communicate with.
Workers have a specific type of message (WorkerMsg), that inherits from ConfigMsg, that have an attribute to distinguish between the 2 different 
workers in the configuration part.

### Publisher

This actor sends PublishMsg to the Broker.

### Subscriber

This actor sends SubscribeMsg to the Broker for a topic and receives directly from the workers the notification messages.

### Worker

This actor receives subscription messages for a topic from the broker and stores subscription in a HashMap.
Upon a PublishMsg is received:
    - if the topic of the received message matches one of the topics stored in HashMap, it sends a NotifyMsg to all subscribers subscribed to that specific topic.
    - otherwise, it simulates a fault. Its crash is handled by the broker, which is ts supervisor, who resumes it.

### Broker

This actor receives both PublishMsg and SubscribeMsg.
Upon receiving a PublishMsg, it forwards it to both the workers that handle them using the policy specified above.
Upon receiving a SubscribeMsg, it forwards it to the corresponding worker depending on the value of the key (even/odd).

Normally, the context of the broker is set to notIsOn().

If a broker receives a BatchMsg:
-  if the boolean isOn is true, the context is changed in isOn, all the messages are processed normally except for PublishMsg that are stashed, by calling the method onPublishBatch().
-  if the boolean isOn is false, the context is changed in notIsOn, the broker turn in its normal state, it un-stashes all the PublishMsg stashed in BatchMode, all the messages are processed as before and PublishMsg are processed by calling the method onPublishBatchOff().

