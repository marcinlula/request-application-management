package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.model.Request;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
interface RequestEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", source = "id")
    void updateEntity(Request request, @MappingTarget RequestEntity entity);

    @Mapping(target = "id", source = "uuid")
    Request toDomain(RequestEntity entity);
}
