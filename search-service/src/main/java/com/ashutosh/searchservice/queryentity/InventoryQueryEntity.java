package com.ashutosh.searchservice.queryentity;

import java.math.BigDecimal;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Data
@Document(indexName = "inventory")
@Setting(settingPath = "elasticsearch/es-settings.json")
public class InventoryQueryEntity {

  @Id
  @Field(type = FieldType.Keyword)
  private String id;

  @Field(type = FieldType.Integer)
  private Integer quantity;

  @Field(type = FieldType.Text, analyzer = "edge_ngram_analyzer")
  private String productName;

  @Field(type = FieldType.Float)
  private BigDecimal price;
}
