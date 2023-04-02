package com.lab;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import com.exercise5.ClientActor;
import com.exercise5.ServerActor;
import com.exercise5.SupervisorActor;
import com.exercise5.messages.InitMsg;

import static java.util.concurrent.TimeUnit.SECONDS;

public class PubSub {

	public final static String TOPIC0 = "topic0";
	public final static String TOPIC1 = "topic1";

	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef broker = sys.actorOf(Broker.props(), "broker");
		final ActorRef subscriber = sys.actorOf(Subscriber.props(), "subscriber");
		final ActorRef publisher = sys.actorOf(Publisher.props(), "publisher");

		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

		// Asking the supervisor to create the server
		scala.concurrent.Future<Object> waitingWorker1 = Patterns.ask(broker, Props.create(Worker.class), 5000);
		ActorRef worker1 = null;
		try {
			worker1 = (ActorRef) waitingWorker1.result(timeout, null);
		} catch (TimeoutException | InterruptedException e) {
			e.printStackTrace();
		}

		scala.concurrent.Future<Object> waitingWorker2 = Patterns.ask(broker, Props.create(Worker.class), 5000);
		ActorRef worker2 = null;
		try {
			worker2 = (ActorRef) waitingWorker2.result(timeout, null);
		} catch (TimeoutException | InterruptedException e) {
			e.printStackTrace();
		}

		publisher.tell(new ConfigMsg(broker), ActorRef.noSender());
		subscriber.tell(new ConfigMsg(broker), ActorRef.noSender());

		broker.tell(new WorkerMsg(worker1, 1), ActorRef.noSender());
		broker.tell(new WorkerMsg(worker2, 2), ActorRef.noSender());

		// Some example subscriptions
		subscriber.tell(new SubscribeMsg(TOPIC0, subscriber), ActorRef.noSender());
		subscriber.tell(new SubscribeMsg(TOPIC1, subscriber), ActorRef.noSender());
		
		// Waiting for subscriptions to propagate
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		publisher.tell(new PublishMsg(TOPIC0, "Test event 1"), ActorRef.noSender());
		publisher.tell(new PublishMsg(TOPIC1, "Test event 2"), ActorRef.noSender());
		
		// Waiting for events to propagate
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Turn the broker in batching mode
		broker.tell(new BatchMsg(true), ActorRef.noSender());

		// Waiting for messages to propagate
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// More example events
		publisher.tell(new PublishMsg(TOPIC0, "Test message 3"), ActorRef.noSender());
		publisher.tell(new PublishMsg(TOPIC1, "Test message 4"), ActorRef.noSender());
		
		// Waiting for events to propagate
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		broker.tell(new BatchMsg(false), ActorRef.noSender());
		// In this example, the last two events shall not be processed until after this point

		// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sys.terminate();
	}

}
