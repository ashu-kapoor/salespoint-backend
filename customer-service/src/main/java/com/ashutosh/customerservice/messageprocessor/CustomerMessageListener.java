package com.ashutosh.customerservice.messageprocessor;

import com.ashutosh.customerservice.service.CustomerService;
import com.ashutosh.saga.kafka.receiver.MessageReceiver;
import com.ashutosh.saga.ordersaga.OrderSagaRequestPayload;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerMessageListener {

  private final CustomerService customerService;
  private final MessageReceiver<OrderSagaRequestPayload> sagaReceiver =
      new MessageReceiver<>("CUSTOMER", "CUSTOMER");

  @EventListener(ApplicationStartedEvent.class)
  public void onMessage() {
    sagaReceiver
        .getReceiver()
        .receive()
        .log()
        .concatMap(this::processAndCommit)
        .doFirst(
            () -> log.info("SubscriptionStarted on thread: {}", Thread.currentThread().getName()))
        .subscribe();
  }

  private Mono<Void> processAndCommit(
      ReceiverRecord<String, OrderSagaRequestPayload> receiverRecord) {

    return Mono.just(receiverRecord)
        .doOnNext(
            r -> {
              log.info(
                  "Message received key:{} topic:{} partition:{}",
                  r.key(),
                  r.topic(),
                  r.partition());
              log.info("Customer processing Saga payload {}", r.value());
            })
        .flatMap(record -> customerService.processSagaCommand(record.value()))
        .retryWhen(retrySpecDBLockFailure())
        .retryWhen(retrySpecDBDown())
        .doOnError(e -> log.error("Error while processing record {}", e.getMessage()))
        .doOnSuccess(e -> receiverRecord.receiverOffset().acknowledge())
        // Add Dead-letter queue if required
        // .onErrorComplete()
        .onErrorResume(
            IndexOutOfBoundsException.class,
            ex -> Mono.fromRunnable(() -> receiverRecord.receiverOffset().acknowledge()))
        .then();
  }

  private static Retry retrySpecDBDown() {
    return Retry.fixedDelay(3, Duration.ofMillis(1))
        .filter(IndexOutOfBoundsException.class::isInstance)
        .onRetryExhaustedThrow((spec, signal) -> signal.failure());
  }

  private static Retry retrySpecDBLockFailure() {
    return Retry.fixedDelay(Long.MAX_VALUE, Duration.ofMillis(5))
        .filter(OptimisticLockingFailureException.class::isInstance)
        .onRetryExhaustedThrow((spec, signal) -> signal.failure());
  }
}
