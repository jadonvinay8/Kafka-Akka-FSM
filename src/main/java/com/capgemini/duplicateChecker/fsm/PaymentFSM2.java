//package com.capgemini.duplicateChecker.fsm;
//
//import akka.actor.AbstractFSM;
//import akka.actor.ActorRef;
//import com.capgemini.duplicateChecker.consumer.DuplicateCheckerReciever;
//import com.capgemini.duplicateChecker.fsm.Events.*;
//import com.capgemini.duplicateChecker.model.PaymentInfo;
//import com.capgemini.duplicateChecker.producer.ServerGenerator;
//import reactor.kafka.receiver.KafkaReceiver;
//
//import java.time.Duration;
//import java.util.Objects;
//import java.util.UUID;
//
//import static com.capgemini.duplicateChecker.fsm.PaymentFSM2.PaymentState.*;
//
//public class PaymentFSM2 extends AbstractFSM<PaymentFSM2.PaymentState, PaymentFSM2.PaymentData> {
//
//    final static KafkaReceiver<String, PaymentInfo> kafkaReceiver = DuplicateCheckerReciever.getReceiver();
//    final static ServerGenerator kafkaProducer = ServerGenerator.create();
//    private static final String DUPLICATION_SUBJECTS_TOPIC = "PaymentTestTopic2";
//
//    interface PaymentData {
//    }
//
//    enum Uninitialized implements PaymentData {
//        UNINITIALIZED
//    }
//
//    public static final class Payment implements PaymentData {
//        public final ActorRef ref;
//        public final String from;
//        public final String to;
//        public final Double amount;
//
//        public Payment(String from, String to, Double amount, ActorRef ref) {
//            this.from = from;
//            this.to = to;
//            this.amount = amount;
//            this.ref = ref;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//            Payment payment = (Payment) o;
//            return Objects.equals(ref, payment.ref) &&
//              Objects.equals(from, payment.from) &&
//              Objects.equals(to, payment.to) &&
//              Objects.equals(amount, payment.amount);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(ref, from, to, amount);
//        }
//
//        @Override
//        public String toString() {
//            return "Payment{" +
//              "ref=" + ref +
//              ", from='" + from + '\'' +
//              ", to='" + to + '\'' +
//              ", amount=" + amount +
//              '}';
//        }
//    }
//
//    public static final class TransformedPayment implements PaymentData {
//        public final UUID transactionId;
//        public final Payment payment;
//
//        public TransformedPayment(UUID transactionId, Payment payment) {
//            this.transactionId = transactionId;
//            this.payment = payment;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//            TransformedPayment that = (TransformedPayment) o;
//            return Objects.equals(transactionId, that.transactionId) &&
//              Objects.equals(payment, that.payment);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(transactionId, payment);
//        }
//
//        @Override
//        public String toString() {
//            return "TransformedPayment{" +
//              "transactionId=" + transactionId +
//              ", payment=" + payment +
//              '}';
//        }
//    }
//
//    enum PaymentState {
//        IDLE,
//        INITIATING
//    }
//
//    {
//        startWith(IDLE, Uninitialized.UNINITIALIZED);
//
//        when(
//          IDLE,
//          matchEvent(
//            PaymentInitiated.class,
//            Uninitialized.class,
//            (event, ignored) -> goTo(INITIATING)
//              .using(event.payment)
//          )
//        );
//
//        when(
//          INITIATING,
//          matchEvent(
//            String.class,
//            Payment.class,
//            (a, b) -> goTo(IDLE).using(Uninitialized.UNINITIALIZED)
//          )
//        );
//
//
//        whenUnhandled(
//          matchAnyEvent(
//            (state, data) -> stay().replying("received unhandled request " + state.toString())
//          )
//        );
//
//        onTransition(
//          matchState(IDLE, INITIATING, () -> {
//              System.out.println("Initiating");
//              Payment data = ((Payment) nextStateData());
//              data.ref.tell("Finished", getSelf());
//          }));
//
//        initialize();
//
//    }
//}