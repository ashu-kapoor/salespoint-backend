package com.ashutosh.saga.framework;

import com.ashutosh.saga.framework.saga.Saga;
import com.ashutosh.saga.framework.saga.SagaEntity;
import com.ashutosh.saga.framework.saga.SagaResponsePayload;
import com.ashutosh.saga.framework.step.Step;
import com.ashutosh.saga.framework.step.StepStage;
import com.ashutosh.saga.framework.step.StepStatus;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class SagaManager<
    E extends SagaEntity, P extends SagaResponsePayload, T extends SagaRepository<E>> {

  public Mono<Void> manageSaga(P responsePayload, T sagaRepository) {
    return sagaRepository
        .findById(responsePayload.getSagaId())
        .filter(getSagaIdempotencyFilter(responsePayload))
        .map(getSagaProcessingFunction(responsePayload))
        .flatMap(sagaRepository::updateSaga)
        .doOnNext(
            n ->
                log.info(
                    "Processed Saga with response command: {}, response status: {}, response sagaID: {}",
                    responsePayload.getCommand(),
                    responsePayload.getStatus(),
                    responsePayload.getSagaId()))
        .then();
  }

  private Function<E, E> getSagaProcessingFunction(P responsePayload) {
    return sagaEntity -> {
      Saga entitySaga = sagaEntity.getSaga();
      Integer currentStepIndex = entitySaga.getCurrentStep();
      Step currentStep = entitySaga.getSteps().get(currentStepIndex);
      if (entitySaga.getSteps().size() - 1 == currentStepIndex
          && responsePayload.getStatus() == StepStatus.SUCCESS) {
        // if success returned and last step
        // complete the saga and update entity
        log.info("Success received for last saga step, completing Saga {}", entitySaga.getId());
        currentStep.setReason(responsePayload.getReason());
        currentStep.setStepStatus(StepStatus.SUCCESS);
        sagaEntity.processCompletion();
      } else if (0 == currentStepIndex && responsePayload.getStatus() == StepStatus.FAILURE) {
        // if failure returned and first step
        // complete the saga and update entity
        log.info(
            "Error received for initial saga step, completing Saga with error {}",
            entitySaga.getId());
        currentStep.setReason(responsePayload.getReason());
        currentStep.setStepStatus(StepStatus.FAILURE);
        sagaEntity.processFailure();
      } else if (responsePayload.getStatus() == StepStatus.FAILURE
          && currentStep.getStepStage() == StepStage.FORWARD) {
        // run previous compensate i.e compensate -1 when current forwarding command responded with
        // failure
        log.info(
            "Current command failed, setting to run previous compensate command for saga {}",
            entitySaga.getId());
        currentStep.setStepStatus(StepStatus.FAILURE);
        entitySaga.setCurrentStep(currentStepIndex - 1);
        Step previousStep = entitySaga.getSteps().get(currentStepIndex - 1);
        previousStep.setStepStage(StepStage.COMPENSATE);
        String topic = previousStep.getChannel();
        Command command = previousStep.getStepInfo().get(StepStage.COMPENSATE);
        entitySaga.getPayload().setCommand(command.command());
        entitySaga.setCurrentChannel(topic);
        currentStep.setReason(responsePayload.getReason());
      } else if (responsePayload.getStatus() == StepStatus.SUCCESS) {
        if (currentStep.getStepStage() == StepStage.FORWARD) {
          // if current step was forwarding and command returned success forward+1
          log.info(
              "Current forwarding command succeeded, setting to run next forward command for saga {}",
              entitySaga.getId());
          currentStep.setStepStatus(StepStatus.SUCCESS);
          currentStep.setReason(responsePayload.getReason());
          entitySaga.setCurrentStep(currentStepIndex + 1);
          Step nextStep = entitySaga.getSteps().get(currentStepIndex + 1);
          nextStep.setStepStage(StepStage.FORWARD);
          String topic = nextStep.getChannel();
          Command command = nextStep.getStepInfo().get(StepStage.FORWARD);
          entitySaga.getPayload().setCommand(command.command());
          entitySaga.setCurrentChannel(topic);
        } else if (currentStep.getStepStage() == StepStage.COMPENSATE) {
          // if current step was compensating, compensate -1 if exists else complete
          if (currentStepIndex == 0) {
            log.info(
                "Current compensating command succeeded, completing saga {}", entitySaga.getId());
            currentStep.setReason(responsePayload.getReason());
            currentStep.setStepStatus(StepStatus.SUCCESS);
            sagaEntity.processFailure();
          } else {
            log.info(
                "Current compensating command succeeded, running previous compensating command for saga {}",
                entitySaga.getId());
            currentStep.setStepStatus(StepStatus.SUCCESS);
            currentStep.setReason(responsePayload.getReason());
            entitySaga.setCurrentStep(currentStepIndex - 1);
            Step previousStep = entitySaga.getSteps().get(currentStepIndex - 1);
            previousStep.setStepStage(StepStage.COMPENSATE);
            String topic = previousStep.getChannel();
            Command command = previousStep.getStepInfo().get(StepStage.COMPENSATE);
            entitySaga.getPayload().setCommand(command.command());
            entitySaga.setCurrentChannel(topic);
          }
        }
      }

      return sagaEntity;
    };
  }

  private Predicate<E> getSagaIdempotencyFilter(P responsePayload) {
    // return item->
    // item.getSaga().getSteps().get(item.getSaga().getCurrentStep()).getChannel().equalsIgnoreCase(responsePayload.getCommand().toString());
    return item ->
        item.getSaga()
            .getPayload()
            .getCommand()
            .equalsIgnoreCase(responsePayload.getCommand().toString());
  }
}
