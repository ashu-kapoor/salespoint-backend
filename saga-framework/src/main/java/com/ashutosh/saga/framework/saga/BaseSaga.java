package com.ashutosh.saga.framework.saga;

import com.ashutosh.saga.framework.step.BaseStep;
import com.ashutosh.saga.framework.step.StepStage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Getter
@ToString
@Setter
public class BaseSaga<T extends SagaPayload, V extends SagaEntity> implements Saga {
  private String id;
  private Integer currentStep;

  private String currentChannel;
  private T payload;

  @JsonIgnore private List<BaseStep<T, V>> steps;

  public BaseSaga() {}

  private BaseSaga(Builder<T, V> builder) {
    id = builder.id;
    currentStep = builder.currentStep;
    payload = builder.payload;
    steps = builder.steps;
    currentChannel = builder.currentChannel;
  }

  public void setCurrentStep(Integer currentStep) {
    this.currentStep = currentStep;
  }

  public List<BaseStep<T, V>> getSteps() {
    return this.steps;
  }

  public static class Builder<T extends SagaPayload, V extends SagaEntity> {
    private final String id;
    private Integer currentStep;

    private String currentChannel;

    private T payload;
    private List<BaseStep<T, V>> steps = new ArrayList<>();

    private Builder(V entityObject, T payload) {
      this.currentStep = 0;
      id = entityObject.getId();
      this.payload = payload;
    }

    public static <T extends SagaPayload, V extends SagaEntity> Builder<T, V> newInstance(
        V entityObject, T payload) {
      return new Builder<>(entityObject, payload);
    }

    public BaseStep<T, V> withStep(String channel) {
      BaseStep<T, V> step = new BaseStep<>(channel);
      step.setSagaBuilder(this);
      this.steps.add(step);
      if (this.steps.size() == 1) {
        this.currentChannel = step.getChannel();
        step.setStepStage(StepStage.FORWARD);
      }
      return step;
    }

    public BaseSaga<T, V> build() {
      return new BaseSaga<>(this);
    }
  }
}
