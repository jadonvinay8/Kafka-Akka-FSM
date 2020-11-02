package com.capgemini.duplicateChecker.deserializer;

import com.capgemini.duplicateChecker.model.PaymentInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class PaymentDeserializer implements Deserializer<PaymentInfo> {
    @Override
    public PaymentInfo deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        PaymentInfo payment = null;
        try {
            payment = mapper.readValue(bytes, PaymentInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return payment;
    }
}
