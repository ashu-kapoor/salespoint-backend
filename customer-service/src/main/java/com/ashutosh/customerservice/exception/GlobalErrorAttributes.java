package com.ashutosh.customerservice.exception;

import org.springframework.http.HttpStatus;

record ExceptionRule(Class<?> exceptionClass, HttpStatus status) {}

public class GlobalErrorAttributes {} /*extends DefaultErrorAttributes {



    private final List<ExceptionRule> exceptionsRules = List.of(
            new ExceptionRule(ValidationException.class, HttpStatus.BAD_REQUEST),
            new ExceptionRule(ResourceNotFoundException.class, HttpStatus.NOT_FOUND),
            new ExceptionRule(DuplicateKeyException.class, HttpStatus.BAD_REQUEST),
            new ExceptionRule(RuntimeException.class, HttpStatus.INTERNAL_SERVER_ERROR)
    );


    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = getError(request);

        Optional<ExceptionRule> exceptionRuleOptional = exceptionsRules.stream()
                .map(exceptionRule -> exceptionRule.exceptionClass().isInstance(error) ? exceptionRule : null)
                .filter(Objects::nonNull)
                .findFirst();

        return exceptionRuleOptional.map(exceptionRule -> Map.of(ErrorAttributesKey.CODE.getKey(), exceptionRule.status().value(), ErrorAttributesKey.MESSAGE.getKey(), error.getMessage(),  ErrorAttributesKey.TIME.getKey(), timestamp))
                .orElseGet(() -> Map.of(ErrorAttributesKey.CODE.getKey(), determineHttpStatus(error).value(),  ErrorAttributesKey.MESSAGE.getKey(), error.getMessage(), ErrorAttributesKey.TIME.getKey(), timestamp));
    }


    private HttpStatus determineHttpStatus(Throwable error) {
        return error instanceof ResponseStatusException err ? err.getStatus() : MergedAnnotations.from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class).getValue(ErrorAttributesKey.CODE.getKey(), HttpStatus.class).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}*/
