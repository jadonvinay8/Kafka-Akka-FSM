package com.capgemini.duplicateChecker.fsm;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import com.capgemini.duplicateChecker.consumer.DuplicateCheckerReciever;
import com.capgemini.duplicateChecker.fsm.Events.*;
import com.capgemini.duplicateChecker.model.PaymentInfo;
import com.capgemini.duplicateChecker.producer.ServerGenerator;
import reactor.kafka.receiver.KafkaReceiver;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import static com.capgemini.duplicateChecker.fsm.PaymentFSM.PaymentState.*;

public class PaymentFSM extends AbstractFSM<PaymentFSM.PaymentState, PaymentFSM.PaymentData> {

    final static KafkaReceiver<String, PaymentInfo> kafkaReceiver = DuplicateCheckerReciever.getReceiver();
    final static ServerGenerator kafkaProducer = ServerGenerator.create();
    private static final String DUPLICATION_SUBJECTS_TOPIC = "PaymentTestTopic2";

    interface PaymentData {
    }

    enum Uninitialized implements PaymentData {
        UNINITIALIZED
    }

    public static final class Payment implements PaymentData {
        public final ActorRef ref;
        public final String from;
        public final String to;
        public final Double amount;

        public Payment(String from, String to, Double amount, ActorRef ref) {
            this.from = from;
            this.to = to;
            this.amount = amount;
            this.ref = ref;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Payment payment = (Payment) o;
            return Objects.equals(ref, payment.ref) &&
              Objects.equals(from, payment.from) &&
              Objects.equals(to, payment.to) &&
              Objects.equals(amount, payment.amount);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ref, from, to, amount);
        }

        @Override
        public String toString() {
            return "Payment{" +
              "ref=" + ref +
              ", from='" + from + '\'' +
              ", to='" + to + '\'' +
              ", amount=" + amount +
              '}';
        }
    }

    public static final class TransformedPayment implements PaymentData {
        public final UUID transactionId;
        public final Payment payment;

        public TransformedPayment(UUID transactionId, Payment payment) {
            this.transactionId = transactionId;
            this.payment = payment;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransformedPayment that = (TransformedPayment) o;
            return Objects.equals(transactionId, that.transactionId) &&
              Objects.equals(payment, that.payment);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transactionId, payment);
        }

        @Override
        public String toString() {
            return "TransformedPayment{" +
              "transactionId=" + transactionId +
              ", payment=" + payment +
              '}';
        }
    }

    enum PaymentState {
        IDLE,
        INITIATING,
        FRAUD_VERIFICATION,
        PROCESS_PAYMENT,
        PAYMENT_COMPLETE,
        TERMINATE
    }

    {
        startWith(IDLE, Uninitialized.UNINITIALIZED);

        when(
          IDLE,
          matchEvent(
            Events.PaymentInitiated.class,
            Uninitialized.class,
            (event, ignored) -> goTo(INITIATING)
              .using(event.payment)
          )
        );

        when(
          INITIATING,
          matchEvent(
            TransactionIDGenerated.class,
            Payment.class,
            (event, ignored) -> goTo(FRAUD_VERIFICATION)
              .using(event.payment)
          )
            .event(PaymentValidationFailed.class,
              ((paymentValidationFailed, paymentData) -> goTo(TERMINATE)))
        );

        when(
          FRAUD_VERIFICATION,
          matchEvent(
            FraudVerificationFailed.class,
            TransformedPayment.class,
            (a, b) -> goTo(TERMINATE)
          )
            .event(
              FraudVerificationSucceeded.class,
              (fraudVerificationSucceeded, paymentData) -> goTo(PROCESS_PAYMENT)
            )
        );

        when(
          PROCESS_PAYMENT,
          matchEvent(
            TransactionSucceeded.class,
            TransformedPayment.class,
            (event, ignored) -> goTo(PAYMENT_COMPLETE)
          )
            .event(
              TransactionFailed.class,
              ((transactionFailed, paymentData) -> goTo(TERMINATE))
            )
        );

        when(
          PAYMENT_COMPLETE,
          matchEvent(
            ConfirmationSent.class,
            TransformedPayment.class,
            (event, ignored) -> goTo(IDLE)
          )
        );

        when(
          TERMINATE,
          matchEvent(
            NegativeAcknowledgementSent.class,
            TransformedPayment.class,
            (a, b) -> goTo(IDLE)
          )
        );

        when(
          TERMINATE,
          matchEvent(
            NegativeAcknowledgementSent.class,
            Payment.class,
            (event, ignored) -> goTo(IDLE)
          )
        );

        whenUnhandled(
          matchAnyEvent(
            (state, data) -> stay().replying("received unhandled request " + state.toString())
          )
        );

        onTransition(
          matchState(IDLE, INITIATING, () -> {
              System.out.println("Initiating");
              Payment data = ((Payment) nextStateData());

              // ADD MESSAGE TO THE TOPIC
              kafkaProducer.generateMessages(new PaymentInfo(data.from, data.to, data.amount), DUPLICATION_SUBJECTS_TOPIC);

              System.out.println(kafkaReceiver);
              kafkaReceiver.receive().delayElements(Duration.ofSeconds(1))
                .doOnNext(msg -> {
                  if (Math.random() > 0.5) {
                      System.out.println("Initial Validation Failed...terminating Payment");
                      data.ref.tell(new PaymentValidationFailed(data), getSelf());

                  } else {
                      System.out.println("Validation succeeded...generating transactionId");
                      TransformedPayment transformedPayment = new TransformedPayment(UUID.randomUUID(), data);
                      TransactionIDGenerated event = new TransactionIDGenerated(transformedPayment);
                      data.ref.tell(event, getSelf());

                  }
                  msg.receiverOffset().acknowledge();
              }).subscribe();


          })
            .state(INITIATING, FRAUD_VERIFICATION, () -> {
                System.out.println("Initiating -> Fraud Verification");
                TransformedPayment event = (TransformedPayment) nextStateData();

                if (Math.random() > 0.9) {
                    System.out.println("Verification failed...terminating");
                    ((Payment) stateData()).ref.tell(new FraudVerificationFailed(), getSelf());

                } else {
                    System.out.println("Verification successful...processing payment");
                    ((Payment) stateData()).ref.tell(new FraudVerificationSucceeded(event), getSelf());

                }
            })
            .state(INITIATING, TERMINATE, () -> {
                System.out.println("Initiating -> Terminating");
                ((Payment) stateData()).ref.tell(new NegativeAcknowledgementSent(), getSelf());

            })
            .state(FRAUD_VERIFICATION, PROCESS_PAYMENT, () -> {
                System.out.println("Fraud -> Processing");
                TransformedPayment transformedPayment = (TransformedPayment) stateData();
                if (Math.random() > 0.9) {
                    System.out.println("Transaction Failed...terminating payment");
                    transformedPayment.payment.ref.tell(new TransactionFailed(transformedPayment), getSelf());

                } else {
                    System.out.println("Transaction succeeded..");
                    transformedPayment.payment.ref.tell(new TransactionSucceeded(transformedPayment), getSelf());
                }

            })
            .state(FRAUD_VERIFICATION, TERMINATE, () -> {
                System.out.println("Fraud Verification -> Terminate");
                ((TransformedPayment) stateData()).payment.ref.tell(new NegativeAcknowledgementSent(), getSelf());

            })
            .state(PROCESS_PAYMENT, PAYMENT_COMPLETE, () -> {
                System.out.println("Process Payment -> Payment Completed");
                System.out.println("Confirmation sent, going to IDLE state");
                ((TransformedPayment) stateData()).payment.ref.tell(new ConfirmationSent(), getSelf());

            })
            .state(PROCESS_PAYMENT, TERMINATE, () -> {
                System.out.println("ProcessingPayment -> Terminate");
                ((TransformedPayment) stateData()).payment.ref.tell(new NegativeAcknowledgementSent(), getSelf());

            })
            .state(PAYMENT_COMPLETE, IDLE, () -> System.out.println("Payment Complete -> Idle"))
            .state(TERMINATE, IDLE, () -> System.out.println("Terminate -> Idle"))
        );

        initialize();

    }
}