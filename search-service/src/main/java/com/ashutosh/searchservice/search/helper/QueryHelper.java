package com.ashutosh.searchservice.search.helper;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
import com.ashutosh.searchservice.search.SearchRequestDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryHelper {
  public static MultiMatchQuery.Builder getQueryBuilder(final SearchRequestDto searchRequestDto) {

    List<String> fields = searchRequestDto.getFields();

    return QueryBuilders.multiMatch()
        .query(searchRequestDto.getSearchTerm())
        .type(TextQueryType.CrossFields)
        .fields(fields)
        .operator(Operator.And);
  }

  public static NativeQuery buildSearchRequest(
      final SearchRequestDto searchRequestDto, Optional<String> filter) {

    Query query = null;
    NativeQueryBuilder nativeQueryBuilder = new NativeQueryBuilder();
    NativeQuery nativeQuery;
    Query.Builder queryBuilder = new Query.Builder();

    if (filter.isEmpty() && searchRequestDto == null) {
      nativeQuery = nativeQueryBuilder.withMaxResults(100).build();
    } else if (filter.isEmpty() && searchRequestDto != null) {
      query = queryBuilder.multiMatch(getQueryBuilder(searchRequestDto).build()).build();
      nativeQuery = nativeQueryBuilder.withQuery(query).build();
    } else if (filter.isPresent() && searchRequestDto == null) {
      List<Query> queriesFromFilter = getQueriesFromFilter(filter.get(), null);
      BoolQuery boolQuery = QueryBuilders.bool().must(queriesFromFilter).build();
      nativeQuery = nativeQueryBuilder.withQuery(boolQuery._toQuery()).build();
    } else {
      query = queryBuilder.multiMatch(getQueryBuilder(searchRequestDto).build()).build();
      List<Query> queriesFromFilter = getQueriesFromFilter(filter.get(), query);
      BoolQuery boolQuery = QueryBuilders.bool().must(queriesFromFilter).build();
      nativeQuery = nativeQueryBuilder.withQuery(boolQuery._toQuery()).build();
    }

    return nativeQuery;
  }

  private static List<Query> getQueriesFromFilter(String filter, Query query) {
    List<Query> queries = new ArrayList<>();
    if (null != query) {
      queries.add(query);
    }

    for (String s : filter.split(";")) {
      Operations operation =
          Arrays.stream(Operations.values())
              .filter(op -> s.contains(op.getValue()))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("Incorrect filter, no operation found"));
      String[] filterData = s.split(operation.getValue());
      if (filterData.length != 2) {
        throw new RuntimeException("Incorrect Filter: should be of format fieldNameOPvalue");
      }
      String fieldName = filterData[0];
      String data = filterData[1];

      queries.add(getQueryForOperation(operation, fieldName, data));
    }

    return queries;
  }

  private static Query getQueryForOperation(Operations operation, String fieldName, String data) {
    RangeQuery.Builder builder = new RangeQuery.Builder();
    builder.field(fieldName);
    builder =
        switch (operation) {
          case GREATER_THAN -> builder.gt(JsonData.of(data));
          case LESS_THAN -> builder.lt(JsonData.of(data));
        };
    RangeQuery rangeQuery = builder.build();
    return rangeQuery._toQuery();
  }
}
