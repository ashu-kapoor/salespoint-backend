package com.ashutosh.searchservice.search;

import java.util.List;
import lombok.Data;

@Data
public class SearchRequestDto {
  private List<String> fields;
  private String searchTerm;
}
