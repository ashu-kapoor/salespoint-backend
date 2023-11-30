package com.ashutosh.inventoryservice.mapper;

import com.ashutosh.inventoryservice.entity.InventoryEntity;
import com.ashutosh.inventoryservice.model.CreateInventoryRequest;
import com.ashutosh.inventoryservice.model.CreateInventoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryEntityMapper {
  CreateInventoryResponse toInventoryResponse(InventoryEntity customerEntity);

  InventoryEntity toInventoryEntity(CreateInventoryRequest customer);
}
