package com.capgemini.duplicateChecker.producer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

public class ReactorKafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(ReactorKafkaProducer.class.getName());

    private static final String TOPIC = "PaymentTestTopic";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String CLIENT_ID_CONFIG = "duplicate-checker-producer";

    private final KafkaSender<String, String> sender;

    public ReactorKafkaProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        SenderOptions<String, String> senderOptions = SenderOptions.create(props);
        sender = KafkaSender.create(senderOptions);
    }

    public void close() {
        sender.close();
    }

    public void generateMessages(String key, String message) {
        sender.createOutbound()
                .send(Flux.just(new ProducerRecord<String, String>(TOPIC, key, message)))
                .then()
                .doOnError(e -> {
                    log.error("Send failed", e);
                    System.out.println("Error");
                })
                .doOnSuccess(unused -> System.out.println("Completed"))
                .subscribe();
    }
}
