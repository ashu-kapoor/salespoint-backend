package com.ashutosh.searchservice.queryentity;

import java.math.BigDecimal;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Data
@Document(indexName = "customer")
@Setting(settingPath = "elasticsearch/es-settings.json")
public class CustomerQueryEntity {

  @Id
  @Field(type = FieldType.Keyword)
  private String id;

  @Field(type = FieldType.Text, analyzer="autocomplete",  searchAnalyzer = "search_autocomplete")
  private String firstName;

  @Field(type = FieldType.Text, analyzer="autocomplete", searchAnalyzer = "search_autocomplete")
  private String lastName;

  @Field(type = FieldType.Float)
  private BigDecimal amount;

  @Field(type = FieldType.Text, analyzer="autocomplete", searchAnalyzer = "search_autocomplete")
  private String email;
}
