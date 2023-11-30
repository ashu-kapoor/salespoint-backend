package com.ashutosh.searchservice.queryentity;

import java.math.BigDecimal;
import lombok.Data;
import org.bson.BasicBSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Data
@Document(indexName = "sales")
@Setting(settingPath = "elasticsearch/es-settings.json")
public class SalesQueryEntity {

  @Id
  @Field(type = FieldType.Keyword)
  private String id;

  @Field(type = FieldType.Integer)
  private Integer quantity;

  @Field(type = FieldType.Text)
  private String customerId;

  @Field(type = FieldType.Text)
  private String productId;

  @Field(type = FieldType.Text, analyzer="autocomplete", searchAnalyzer = "search_autocomplete")
  private String status;

  @Field(type = FieldType.Float)
  private BigDecimal amount;

  private CustomerQueryEntity customer;

  private InventoryQueryEntity inventory;

  private BasicBSONObject saga;
}
