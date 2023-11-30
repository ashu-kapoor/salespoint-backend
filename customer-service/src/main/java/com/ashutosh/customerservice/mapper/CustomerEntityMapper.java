package com.ashutosh.customerservice.mapper;

import com.ashutosh.customerservice.entity.CustomerEntity;
import com.ashutosh.customerservice.model.CreateCustomerRequest;
import com.ashutosh.customerservice.model.CreateCustomerResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerEntityMapper {
  CreateCustomerResponse toCustomerResponse(CustomerEntity customerEntity);

  CustomerEntity toCustomerEntity(CreateCustomerRequest customer);
}
