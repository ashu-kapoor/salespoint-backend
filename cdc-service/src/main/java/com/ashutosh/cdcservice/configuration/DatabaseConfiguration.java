package com.ashutosh.cdcservice.configuration;

import com.ashutosh.cdcservice.Model;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("spring.cdc")
@Getter
@Setter
public class DatabaseConfiguration {
  private List<Model> service;
}
