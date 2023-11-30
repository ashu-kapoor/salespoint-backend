package com.ashutosh.saga.framework.step;

import com.ashutosh.saga.framework.*;
import com.ashutosh.saga.framework.saga.BaseSaga;
import com.ashutosh.saga.framework.saga.SagaEntity;
import com.ashutosh.saga.framework.saga.SagaPayload;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

@Getter
@Setter
@ToString
public class BaseStep<T extends SagaPayload, V extends SagaEntity> implements Step {

  public BaseStep(String channel) {
    this.channel = channel;
    this.stepInfo = new HashMap<>();
  }

  private String channel;
  private Map<StepStage, Command> stepInfo;

  private String reason;
  private StepStage stepStage; // Compensating or forwarding
  private StepStatus stepStatus; // Success or failure

  @Transient @ToString.Exclude private BaseSaga.Builder<T, V> sagaBuilder;

  public BaseSaga.Builder<T, V> and() {
    return this.sagaBuilder;
  }

  public BaseStep<T, V> processingCommand(Command command) {
    stepInfo.putIfAbsent(StepStage.FORWARD, command);
    return this;
  }

  public BaseStep<T, V> compensatingCommand(Command command) {
    stepInfo.putIfAbsent(StepStage.COMPENSATE, command);
    return this;
  }
}
