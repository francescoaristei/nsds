package com.lab;

import akka.actor.ActorRef;

public class ConfigMsg {

    private final ActorRef actor;

    public ConfigMsg(ActorRef actor){
        this.actor = actor;
    }

    public ActorRef getActor() {
        return actor;
    }
}
