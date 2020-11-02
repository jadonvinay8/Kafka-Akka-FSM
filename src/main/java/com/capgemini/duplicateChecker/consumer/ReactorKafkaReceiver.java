package com.capgemini.duplicateChecker.consumer;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.capgemini.duplicateChecker.deserializer.PaymentDeserializer;
import com.capgemini.duplicateChecker.fsm.Events;
import com.capgemini.duplicateChecker.fsm.PaymentFSM;
import com.capgemini.duplicateChecker.model.PaymentInfo;
import com.capgemini.duplicateChecker.producer.ReactorKafkaProducer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.*;

@Component
public class ReactorKafkaReceiver {

    private static final Logger log = LoggerFactory.getLogger(ReactorKafkaReceiver.class.getName());
    private ReactorKafkaProducer kafkaProducer = new ReactorKafkaProducer();
    private KafkaReceiver kafkaReceiver;
    private static final String TOPIC = "PaymentTestTopic";

    public ReactorKafkaReceiver() {

        final Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, PaymentDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "duplicate-checker0");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group-id");
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        ReceiverOptions<Object, Object> consumerOptions = ReceiverOptions.create(consumerProps)
                .subscription(Collections.singleton(TOPIC))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));

        kafkaReceiver = KafkaReceiver.create(consumerOptions);
        ActorSystem system = ActorSystem.create("main");

//        List<PaymentInfo> messages = new ArrayList<>();
//
//        messages.add(new PaymentInfo("V1", "V2", 200.0));
//        messages.add(new PaymentInfo("V2", "V3", 2000.0));
//        messages.add(new PaymentInfo("V3", "V4", 200.0));
//        messages.add(new PaymentInfo("V4", "V5", 2000.0));

//        messages.forEach(message -> {
//            final ActorRef ref = system.actorOf(Props.create(PaymentFSM.class));
//            ref.tell(new PaymentInitiated(new Payment(message.from, message.to, message.amount, ref)), ref);
//        });

        final ActorRef ref = system.actorOf(Props.create(PaymentFSM.class));
        ((Flux<ReceiverRecord>) kafkaReceiver.receive())
//                .delayElements(Duration.ofSeconds(1))
                .doOnNext(r -> {
                    System.out.println(r);
                    System.out.println(r.key());
                    System.out.println(r.value());
//                    kafkaProducer.generateMessages(r.value().toString(), "Yes");
                    PaymentInfo message = (PaymentInfo)(r.value());
                    final ActorRef ref1 = system.actorOf(Props.create(PaymentFSM.class));

                    ref1.tell(new Events.PaymentInitiated(new PaymentFSM.Payment(message.getFrom(), message.getTo(), message.getAmount(), ref1)), ref1);
//                    ref.tell(new Events.PaymentInitiated(new PaymentFSM.Payment(message.getFrom(), message.getTo(), message.getAmount(), ref)), ref);
                    r.receiverOffset().acknowledge();
                })
//                .doOnComplete(() -> kafkaProducer.close())
                .subscribe();
    }

}
