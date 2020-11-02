package com.capgemini.duplicateChecker.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
//iso20022
@Data
@NoArgsConstructor
public class PaymentInfo {
//    private String merchantId;
//    private String transactionId;
//    private String merchantUserId;
//    private LocalDateTime timestamp;
//    private double amount;
//    private String merchantOrderId;
//    private String mobileNumber;
//    private String message;
//    private String subMerchant;
//    private String email;
//    private String shortName;
    private String from;
    private String to;
    private Double amount;

    public PaymentInfo(String from, String to, Double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "PaymentInfo{" +
          "from='" + from + '\'' +
          ", to='" + to + '\'' +
          ", amount=" + amount +
          '}';
    }
}
