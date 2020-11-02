package com.capgemini.duplicateChecker.consumer;
import com.capgemini.duplicateChecker.deserializer.PaymentDeserializer;
import com.capgemini.duplicateChecker.model.PaymentInfo;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DuplicateCheckerReciever {


    public static KafkaReceiver<String, PaymentInfo> getReceiver(){

        final String TOPIC = "PaymentTestTopic2";
        final Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, PaymentDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "duplicate-checker");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group-id");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        ReceiverOptions<String, PaymentInfo> consumerOptions = ReceiverOptions.create(consumerProps);

        return KafkaReceiver
          .create(consumerOptions
            .subscription(Collections.singleton(TOPIC)));
//            .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
//            .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions)));
    }
}
