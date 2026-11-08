package com.marcinlula.requestmanagement.api;

import com.marcinlula.requestmanagement.api.dto.RequestDto;
import com.marcinlula.requestmanagement.model.Request;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RequestApiMapper {

    RequestDto map(Request request);
}
