package com.ashutosh.searchservice.messageprocessor;

import com.ashutosh.saga.kafka.receiver.QueryReceiver;
import com.ashutosh.searchservice.service.query.SalesQueryService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
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
public class SalesQueryMessageListener {

  private final SalesQueryService salesQueryService;
  private final QueryReceiver<Document> sagaReceiver =
      new QueryReceiver<>("SALES_QUERY", "sales-query");

  @EventListener(ApplicationStartedEvent.class)
  public void onMessage() {
    sagaReceiver
        .getReceiver()
        .receive()
        // .log()
        .concatMap(this::processAndCommit)
        .doFirst(
            () -> log.info("SubscriptionStarted on thread: {}", Thread.currentThread().getName()))
        .subscribe();
  }

  private Mono<Void> processAndCommit(ReceiverRecord<String, Document> receiverRecord) {

    return Mono.just(receiverRecord)
        .doOnNext(
            r -> {
              log.info(
                  "Message received key:{} topic:{} partition:{}",
                  r.key(),
                  r.topic(),
                  r.partition());
              log.info("Sales query processing Saga payload {}", r.value());
            })
        .flatMap(record -> salesQueryService.processRecord(record.value(), record.key()))
        .retryWhen(retrySpecDBLockFailure())
        .retryWhen(retrySpecDBDown())
        .doOnError(e -> log.error("Error while processing record {}", e.getMessage()))
        .doOnSuccess(e -> receiverRecord.receiverOffset().acknowledge())
        // Add Dead-letter queue if required
        // .onErrorComplete()
        .onErrorResume(
            IndexOutOfBoundsException.class,
            ex -> Mono.fromRunnable(() -> receiverRecord.receiverOffset().acknowledge()))
        .onErrorResume(
            ClassCastException.class,
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
