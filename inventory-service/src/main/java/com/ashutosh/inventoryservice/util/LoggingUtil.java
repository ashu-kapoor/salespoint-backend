package com.ashutosh.inventoryservice.util;

import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import reactor.core.observability.DefaultSignalListener;
import reactor.core.observability.SignalListener;

@Slf4j
public class LoggingUtil {
  public static <T> Supplier<SignalListener<T>> logTapper(StringBuilder logBuilder) {
    return () ->
        new DefaultSignalListener<T>() {
          @Override
          public void doOnComplete() throws Throwable {
            // super.doOnComplete();
            log.info("Success {}", logBuilder.toString());
          }

          @Override
          public void doOnError(Throwable error) throws Throwable {
            // super.doOnError(error);
            log.error("Error  {}, {}", logBuilder.toString(), error.getMessage());
          }
        };
  }
}
