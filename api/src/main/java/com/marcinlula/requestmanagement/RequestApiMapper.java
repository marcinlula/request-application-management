package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.dto.RequestDto;
import com.marcinlula.requestmanagement.model.Request;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RequestApiMapper {

    RequestDto map(Request request);
}
