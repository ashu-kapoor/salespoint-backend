package com.ashutosh.cdcservice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;

@Getter
@Setter
public class Model {
  private String name;
  private MongoProperties mongodb;
}
