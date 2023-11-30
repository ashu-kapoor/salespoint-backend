package com.ashutosh.customerservice.entity;

import com.ashutosh.saga.ordersaga.OrderSagaResponsePayload;
import java.math.BigDecimal;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class CustomerEntity {
  @Id private String id;
  private String firstName;
  private String lastName;
  private BigDecimal amount;

  @Indexed(unique = true)
  private String email;

  @Version Long version;

  private org.bson.Document orderProcessedMap;

  private OrderSagaResponsePayload saga;
}
