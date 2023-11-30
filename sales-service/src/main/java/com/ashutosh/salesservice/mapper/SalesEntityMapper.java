package com.ashutosh.salesservice.mapper;

import com.ashutosh.salesservice.entity.SalesEntity;
import com.ashutosh.salesservice.model.CreateSalesRequest;
import com.ashutosh.salesservice.model.CreateSalesResponse;
import com.ashutosh.salesservice.model.GetSalesResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SalesEntityMapper {
  CreateSalesResponse toCreateSalesResponse(SalesEntity salesEntity);

  GetSalesResponse toGetSalesResponse(SalesEntity salesEntity);

  SalesEntity toSalesEntity(CreateSalesRequest createSalesRequest);
}
