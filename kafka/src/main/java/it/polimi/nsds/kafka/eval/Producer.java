package it.polimi.nsds.kafka.eval;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;

public class Producer {
    private static final String serverAddr = "localhost:9092";
    private static final String topic = "inputTopic";

    private static final int numMessages = 1000000;
    private static final int waitBetweenMsgs = 500;

    private static final int numKeys = 100;
    private static final int minVal = 10;
    private static final int maxVal = 20;

    public static void main(String[] args) {
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);

        // Idempotence = exactly once semantics between the producer and the partition
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());

        final KafkaProducer<String, Integer> producer = new KafkaProducer<>(props);
        final Random r = new Random();

        for (int i = 0; i < numMessages; i++) {
            final String key = "Key" + r.nextInt(numKeys);
            final Integer value = minVal + r.nextInt(maxVal - minVal);

            System.out.println(
                    "Key: " + key +
                    "\tValue: " + value
            );

            final ProducerRecord<String, Integer> record = new ProducerRecord<>(topic, key, value);
            producer.send(record);

            try {
                Thread.sleep(waitBetweenMsgs);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();
    }
}