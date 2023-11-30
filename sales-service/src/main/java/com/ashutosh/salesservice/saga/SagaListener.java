package com.ashutosh.salesservice.saga;

import com.ashutosh.saga.framework.SagaManager;
import com.ashutosh.saga.framework.SagaRepository;
import com.ashutosh.saga.kafka.receiver.SagaReceiver;
import com.ashutosh.saga.ordersaga.OrderSagaResponsePayload;
import com.ashutosh.salesservice.entity.SalesEntity;
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
public class SagaListener {

  private final SagaRepository<SalesEntity> repository;
  private final SagaManager<SalesEntity, OrderSagaResponsePayload, SagaRepository<SalesEntity>>
      processor = new SagaManager<>();
  private final SagaReceiver<OrderSagaResponsePayload> sagaReceiver =
      new SagaReceiver<>("SALES_SAGA");

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
      ReceiverRecord<String, OrderSagaResponsePayload> receiverRecord) {

    return Mono.just(receiverRecord)
        .doOnNext(
            r -> {
              log.info(
                  "Message received key:{} topic:{} partition:{}",
                  r.key(),
                  r.topic(),
                  r.partition());
              log.info("Managing Saga for payload {}", r.value());
            })
        .flatMap(record -> processor.manageSaga(record.value(), repository))
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
