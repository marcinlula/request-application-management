package com.marcinlula.requestmanagement.api;

import com.marcinlula.requestmanagement.api.dto.RequestDto;
import com.marcinlula.requestmanagement.api.dto.StateChangeDto;
import com.marcinlula.requestmanagement.model.Request;
import com.marcinlula.requestmanagement.model.StateChange;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestApiMapper {

    RequestDto map(Request request);

    List<StateChangeDto> mapHistory(List<StateChange> history);
}
