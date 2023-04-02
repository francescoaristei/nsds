package com.lab;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Subscriber extends AbstractActor {

    private ActorRef broker;
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SubscribeMsg.class, this::onSubscribe)
                .match(NotifyMsg.class, this::onNotify)
                .match(ConfigMsg.class, this::onConfig)
                .build();
    }

    private void onNotify(NotifyMsg msg) {
        System.out.println("Subscriber - Message received: " + msg.getValue());
    }

    private void onConfig(ConfigMsg msg) {
        broker = msg.getActor();
    }

    private void onSubscribe(SubscribeMsg msg) {

        System.out.println("Subscriber - Subscription to " + msg.getTopic());
        broker.tell(msg, self());
    }

    public static Props props() {
        return Props.create(Subscriber.class);
    }
}
