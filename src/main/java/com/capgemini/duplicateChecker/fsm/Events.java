package com.capgemini.duplicateChecker.fsm;


import com.capgemini.duplicateChecker.fsm.PaymentFSM.Payment;

import static com.capgemini.duplicateChecker.fsm.PaymentFSM.*;


public class Events {

    public static final class PaymentInitiated {
        public final Payment payment;

        public PaymentInitiated(Payment payment) {
            this.payment = payment;
        }
    }

    static final class PaymentValidationFailed {
        public final Payment payment;

        public PaymentValidationFailed(Payment payment) {
            this.payment = payment;
        }
    }

    public static final class TransactionIDGenerated {
        public final TransformedPayment payment;

        public TransactionIDGenerated(TransformedPayment payment) {
            this.payment = payment;
        }
    }

    static final class FraudVerificationSucceeded {
        public final TransformedPayment payment;

        public FraudVerificationSucceeded(TransformedPayment payment) {
            this.payment = payment;
        }
    }

    static final class FraudVerificationFailed {   }

    static final class TransactionSucceeded {
        public final TransformedPayment payment;

        public TransactionSucceeded(TransformedPayment payment) {
            this.payment = payment;
        }
    }

    static final class TransactionFailed {
        public final TransformedPayment payment;

        public TransactionFailed(TransformedPayment payment) {
            this.payment = payment;
        }
    }

    static final class ConfirmationSent {   }

    static final class NegativeAcknowledgementSent {   }
}
