package com.lab;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Worker extends AbstractActor {

    private Map<String, List<ActorRef>> map = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PublishMsg.class, this::onPublish)
                .match(SubscribeMsg.class, this::onSubscribe)
                .build();
    }

    private void onPublish (PublishMsg msg) throws Exception {


        if (map.containsKey(msg.getTopic())) {
            for (ActorRef a : map.get(msg.getTopic())) {

                System.out.println(this.self() + " - Sending notification: " + msg.getTopic() + " " + msg.getValue());

                a.tell(new NotifyMsg(msg.getValue()), self());
            }
        }
        else {
            throw new Exception(this.self() + " Simulating Fault!");
        }
    }

    private void onSubscribe (SubscribeMsg msg) {

        String topic = msg.getTopic();
        ActorRef actor = msg.getSender();

        if(! map.containsKey(topic)) {
            map.put(topic, new ArrayList<>());
        }

        map.get(topic).add(actor);
    }

    public static Props props() {
        return Props.create(Worker.class);
    }
}
