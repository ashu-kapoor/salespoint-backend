package com.ashutosh.inventoryservice.validations;

import com.ashutosh.inventoryservice.exception.ValidationException;
import com.ashutosh.inventoryservice.model.CreateInventoryRequest;
import io.micrometer.common.util.StringUtils;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.SynchronousSink;

@Component
public class InventoryValidator
    implements BiConsumer<CreateInventoryRequest, SynchronousSink<CreateInventoryRequest>> {

  public static final String PRODUCT_NAME_BLANK = "Product name cant be blank";
  public static final String PRICE_BLANK = "Price cant be blank or negative or zero";
  public static final String QUANTITY = "Price cant be blank or negative or zero";

  @Override
  public void accept(
      CreateInventoryRequest inventoryRequest,
      SynchronousSink<CreateInventoryRequest> synchronousSink) {
    StringJoiner message = new StringJoiner(" , ");
    if (StringUtils.isBlank(inventoryRequest.getProductName())) {
      message.add(PRODUCT_NAME_BLANK);
    }

    Optional.ofNullable(inventoryRequest)
        .map(CreateInventoryRequest::getPrice)
        .filter(p -> p.compareTo(BigDecimal.ZERO) <= 0)
        .ifPresent(a -> message.add(PRICE_BLANK));

    Optional.ofNullable(inventoryRequest)
        .map(CreateInventoryRequest::getQuantity)
        .filter(i -> i <= 0)
        .ifPresent(a -> message.add(QUANTITY));

    String exception = message.toString();
    if (StringUtils.isNotBlank(exception)) {
      synchronousSink.error(new ValidationException(exception));
    } else {
      synchronousSink.next(inventoryRequest);
    }
  }

  private boolean validFormat(String email) {
    return true;
  }
}
