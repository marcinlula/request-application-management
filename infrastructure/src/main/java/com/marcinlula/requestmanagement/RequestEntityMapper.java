package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.model.Request;
import com.marcinlula.requestmanagement.model.StateChange;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
interface RequestEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", source = "id")
    @Mapping(target = "history", ignore = true)
    void updateEntity(Request request, @MappingTarget RequestEntity entity);

    @Mapping(target = "id", source = "uuid")
    Request toDomain(RequestEntity entity);

    @Mapping(target = "from", source = "fromState")
    @Mapping(target = "to", source = "toState")
    StateChange toDomain(StateChangeEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "request", ignore = true)
    @Mapping(target = "fromState", source = "from")
    @Mapping(target = "toState", source = "to")
    StateChangeEntity toEntity(StateChange change);
}
