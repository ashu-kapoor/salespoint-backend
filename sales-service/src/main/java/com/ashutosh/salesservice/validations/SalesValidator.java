package com.ashutosh.salesservice.validations;

import com.ashutosh.salesservice.exception.ValidationException;
import com.ashutosh.salesservice.model.CreateSalesRequest;
import io.micrometer.common.util.StringUtils;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.SynchronousSink;

@Component
public class SalesValidator
    implements BiConsumer<CreateSalesRequest, SynchronousSink<CreateSalesRequest>> {

  public static final String PRODUCT_ID_BLANK = "Product Id cant be blank";
  public static final String CUSTOMER_ID_BLANK = "Customer Id cant be blank";
  public static final String QUANTITY = "Quantity cant be blank or negative or zero";

  @Override
  public void accept(
      CreateSalesRequest createSalesRequest, SynchronousSink<CreateSalesRequest> synchronousSink) {
    StringJoiner message = new StringJoiner(" , ");

    Optional<CreateSalesRequest> salesRequest = Optional.ofNullable(createSalesRequest);

    salesRequest
        .map(CreateSalesRequest::getQuantity)
        .filter(p -> p <= 0)
        .ifPresent(a -> message.add(QUANTITY));

    salesRequest
        .map(CreateSalesRequest::getCustomerId)
        .filter(StringUtils::isBlank)
        .ifPresent(a -> message.add(CUSTOMER_ID_BLANK));

    salesRequest
        .map(CreateSalesRequest::getProductId)
        .filter(StringUtils::isBlank)
        .ifPresent(a -> message.add(PRODUCT_ID_BLANK));

    String exception = message.toString();
    if (StringUtils.isNotBlank(exception)) {
      synchronousSink.error(new ValidationException(exception));
    } else {
      synchronousSink.next(createSalesRequest);
    }
  }
}
