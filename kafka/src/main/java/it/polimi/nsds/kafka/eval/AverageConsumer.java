package it.polimi.nsds.kafka.eval;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AverageConsumer {
    private static final String serverAddr = "localhost:9092";
    private static final String inputTopic = "inputTopic";

    private static final int autoCommitIntervalMs = 15000;
    private static final String offsetResetStrategy = "latest";

    private static float meanBefore = 0;

    public static void main(String[] args) {
        final Properties props = new Properties();
        final String consumerGroupId = args [0];

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(true));
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());

        KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(props);

        //the consumer subscribes to the topics' list
        consumer.subscribe(Collections.singletonList(inputTopic));

        Map<String, Integer> map = new HashMap<>();

        while (true) {
            final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

            for (final ConsumerRecord<String, Integer> record : records) {

                map.put(record.key(), record.value());

                float meanAfter = mean(map);

                System.out.println(meanBefore);

                System.out.print("Consumer group: " + consumerGroupId + "\t");
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tValue: " + record.value()
                );

                if(meanAfter != meanBefore) {
                    System.out.println("Mean updated: " + meanAfter);
                    meanBefore = meanAfter;
                }

            }

        }
    }

    public static float mean(Map<String, Integer> map){

        float sum = 0;

        for (String s : map.keySet()){
            sum += map.get(s);
        }

        return sum / map.keySet().size();
    }
}