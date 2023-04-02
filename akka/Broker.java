package com.lab;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;

public class Broker extends AbstractActorWithStash {

    private ActorRef worker1;
    private ActorRef worker2;

    private static final SupervisorStrategy strategy =
            new OneForOneStrategy(
                    1, // Max no of retries
                    Duration.ofMinutes(1), // Within what time period
                    DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume())
                            .build());

    public static Props props() {
        return Props.create(Broker.class);
    }


    @Override
    public Receive createReceive() {
        return notIsOn();
    }

    public Receive isOn(){
        return receiveBuilder()
                .match(BatchMsg.class, this::onBatch)
                .match(PublishMsg.class, this::onPublishBatch)
                .match(WorkerMsg.class, this::onWorker)
                .match(SubscribeMsg.class, this::onSubscribe)
                .match(Props.class,
                        props -> {
                            getSender().tell(getContext().actorOf(props), getSelf());
                        })
                .build();
    }

    private Receive notIsOn() {
        return receiveBuilder()
                .match(BatchMsg.class, this::onBatch)
                .match(PublishMsg.class, this:: onPublishBatchOff)
                .match(WorkerMsg.class, this::onWorker)
                .match(SubscribeMsg.class, this::onSubscribe)
                .match(Props.class,
                        props -> {
                            getSender().tell(getContext().actorOf(props), getSelf());
                        })
                .build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    private void onWorker(WorkerMsg msg) {

        if(msg.getWorkerNumber() == 1)
            worker1 = msg.getActor();
        else {
            worker2 = msg.getActor();
        }
    }

    private void onBatch (BatchMsg msg) {

        if (! msg.isOn()) {
            unstashAll();
            getContext().become(notIsOn());
        }
        else {
            getContext().become(isOn());
        }
    }

    private void onPublishBatch (PublishMsg msg) {

        System.out.println("Broker - Stashing msg: " + msg.getTopic());

        stash();
    }

    private void onPublishBatchOff (PublishMsg msg) {

        System.out.println("Broker - Forwarding message to workers");

        worker1.tell(msg, self());
        worker2.tell(msg, self());
    }

    private void onSubscribe (SubscribeMsg msg) {

        System.out.println("Broker - Forwarding subscription to workers");

        if ((msg.getKey() % 2) == 0) {
            worker2.tell(msg, self());
        }
        else{
            worker1.tell(msg, self());
        }
    }
}
