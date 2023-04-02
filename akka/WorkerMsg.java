package com.lab;

import akka.actor.ActorRef;

public class WorkerMsg extends ConfigMsg{

    int workerNumber;

    public WorkerMsg(ActorRef actor, int workerNumber) {
        super(actor);
        this.workerNumber = workerNumber;
    }

    public int getWorkerNumber() {
        return workerNumber;
    }
}
