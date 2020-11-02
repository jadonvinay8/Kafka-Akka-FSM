package com.capgemini.duplicateChecker.controller;

import com.capgemini.duplicateChecker.model.PaymentInfo;
import com.capgemini.duplicateChecker.producer.ServerGenerator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaController {

    private static final String TOPIC = "PaymentTestTopic";
    private static final ServerGenerator sse= ServerGenerator.create();

    @PostMapping
    void addPayment(@RequestBody PaymentInfo payment){

        sse.generateMessages(payment, TOPIC);
    }

}
