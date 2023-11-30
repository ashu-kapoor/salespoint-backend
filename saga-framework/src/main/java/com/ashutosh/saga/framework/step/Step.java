package com.ashutosh.saga.framework.step;

import com.ashutosh.saga.framework.Command;
import java.util.Map;

public interface Step {
  String getChannel();

  Map<StepStage, Command> getStepInfo();

  String getReason();

  StepStage getStepStage(); // Compensating or forwarding

  StepStatus getStepStatus(); // Success or failure

  void setStepStage(StepStage stepStage);

  void setReason(String reason);

  void setStepStatus(StepStatus stepStatus);
}
