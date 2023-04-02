package com.lab;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Publisher extends AbstractActor {

    private ActorRef broker;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PublishMsg.class, this::onPublish)
                .match(ConfigMsg.class, this::onConfig)
                .build();
    }

    private void onPublish (PublishMsg msg) {

        System.out.println("Publisher - Sent message " + msg.getTopic() + " " + msg.getValue());

        broker.tell(msg, ActorRef.noSender());
    }

    private void onConfig (ConfigMsg msg) {
        broker = msg.getActor();
    }

    public static Props props() {
        return Props.create(Publisher.class);
    }
}
