package com.ashutosh.cdcservice.om;

import com.ashutosh.saga.framework.saga.SagaPayload;
import com.ashutosh.saga.ordersaga.OrderSagaRequestPayload;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class SagaPayloadDeserializer extends StdDeserializer<SagaPayload> {
  public SagaPayloadDeserializer() {
    super(SagaPayload.class);
  }

  public SagaPayload deserialize(JsonParser jsonParser, DeserializationContext context)
      throws IOException {
    return jsonParser.readValueAs(OrderSagaRequestPayload.class);
  }
}
