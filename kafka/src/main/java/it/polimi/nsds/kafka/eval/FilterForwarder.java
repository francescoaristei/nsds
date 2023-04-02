package it.polimi.nsds.kafka.eval;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class FilterForwarder {
    private static final String serverAddr = "localhost:9092";

    private static final String inputTopic = "inputTopic";
    private static final String outputTopic = "outputTopic";

    public static final int threshold = 15;

    // TODO add attributes here if needed
    private static final String producerTransactionalId = "forwarderTransactionalId";

    public static void main(String[] args) {

        final String consumerGroupId = args [0];

        // Consumer settings
        final Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());

        consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(false));

        KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(inputTopic));

        // Producer settings
        final Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());

        producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, producerTransactionalId);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));

        final KafkaProducer<String, Integer> producer = new KafkaProducer<>(producerProps);
        producer.initTransactions();

        while (true) {
            final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

            producer.beginTransaction();
            for (final ConsumerRecord<String, Integer> record : records) {
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tValue: " + record.value()
                );
                if (record.value() > threshold)
                    producer.send(new ProducerRecord<>(outputTopic, record.key(), record.value()));
            }

            final Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
            for (final TopicPartition partition : records.partitions()) {
                final List<ConsumerRecord<String, Integer>> partitionRecords = records.records(partition);
                final long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                map.put(partition, new OffsetAndMetadata(lastOffset + 1));
            }


            producer.sendOffsetsToTransaction(map, consumer.groupMetadata());
            producer.commitTransaction();
        }
    }
}