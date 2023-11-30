package com.ashutosh.customerservice.validations;

import com.ashutosh.customerservice.exception.ValidationException;
import com.ashutosh.customerservice.model.CreateCustomerRequest;
import io.micrometer.common.util.StringUtils;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.SynchronousSink;

@Component
public class CustomerValidator
    implements BiConsumer<CreateCustomerRequest, SynchronousSink<CreateCustomerRequest>> {

  public static final String FIRST_NAME_BLANK = "First name cant be blank";
  public static final String LAST_NAME_BLANK = "Last name cant be blank";
  public static final String INVALID_EMAIL = "Invalid Email";

  @Override
  public void accept(
      CreateCustomerRequest customerRequest,
      SynchronousSink<CreateCustomerRequest> synchronousSink) {
    StringJoiner message = new StringJoiner(" , ");
    if (StringUtils.isBlank(customerRequest.getFirstName())) {
      message.add(FIRST_NAME_BLANK);
    }
    if (StringUtils.isBlank(customerRequest.getLastName())) {
      message.add(LAST_NAME_BLANK);
    }
    if (StringUtils.isBlank(customerRequest.getEmail())
        || !validFormat(customerRequest.getEmail())) {
      message.add(INVALID_EMAIL);
    }

    String exception = message.toString();
    if (StringUtils.isNotBlank(exception)) {
      synchronousSink.error(new ValidationException(exception));
    } else {
      synchronousSink.next(customerRequest);
    }
  }

  private boolean validFormat(String email) {
    return true;
  }
}
