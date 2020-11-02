package com.capgemini.duplicateChecker.producer;

import com.capgemini.duplicateChecker.model.PaymentInfo;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

public class ServerGenerator {
    private static final Logger log = LoggerFactory.getLogger(ServerGenerator.class.getName());
    private static final String TOPIC = "PaymentTestTopic";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String CLIENT_ID_CONFIG = "trans-string-consumer-egen-new";
    private final KafkaSender<String, PaymentInfo> sender;

    static ServerGenerator sg;

    public static ServerGenerator create() {
        if (sg == null) sg = new ServerGenerator();
        return sg;
    }

    private ServerGenerator() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        SenderOptions<String, PaymentInfo> senderOptions = SenderOptions.create(props);
        sender = KafkaSender.create(senderOptions);
//        dateFormat = new SimpleDateFormat("HH:mm:ss:SSS z dd MMM yyyy");
    }

    public void close() {
        sender.close();
    }

    public void generateMessages(PaymentInfo payment, String topic) {
        System.out.println(payment);
        sender.createOutbound()
          .send(Flux.just(new ProducerRecord<String, PaymentInfo>(topic, "Key_", payment)))
          .then()
          .doOnError(e -> log.error("Send failed", e))
          .subscribe(r -> {
              System.out.print("Message sent successfully");
          });
    }
}
