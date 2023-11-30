package com.ashutosh.saga.framework.saga;

import com.ashutosh.saga.framework.step.Step;
import java.util.List;

public interface Saga {
  void setCurrentStep(Integer currentStep);

  void setCurrentChannel(String channel);

  String getId();

  Integer getCurrentStep();

  List<? extends Step> getSteps();

  SagaPayload getPayload();

  String getCurrentChannel();
}
