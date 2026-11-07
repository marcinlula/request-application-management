package com.marcinlula.requestmanagement;

import com.marcinlula.requestmanagement.dto.CreateRequestDto;
import com.marcinlula.requestmanagement.dto.ReasonDto;
import com.marcinlula.requestmanagement.dto.UpdateBodyDto;
import com.marcinlula.requestmanagement.dto.RequestDto;
import com.marcinlula.requestmanagement.model.Request;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/application-requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final RequestApiMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto create(@Valid @RequestBody CreateRequestDto createRequestDto) {
        Request request = requestService.create(createRequestDto.name(), createRequestDto.body());
        return mapper.map(request);
    }

    @GetMapping("/{id}")
    public RequestDto get(@PathVariable UUID id) {
        Request request = requestService.get(id);
        return mapper.map(request);
    }

    @PatchMapping("/{id}")
    public RequestDto changeBody(@PathVariable UUID id, @Valid @RequestBody UpdateBodyDto request) {
        return mapper.map(requestService.changeBody(id, request.body()));
    }

    @PostMapping("/{id}/verify")
    public RequestDto verify(@PathVariable UUID id) {
        return mapper.map(requestService.verify(id));
    }

    @PostMapping("/{id}/accept")
    public RequestDto accept(@PathVariable UUID id) {
        return mapper.map(requestService.accept(id));
    }

    @PostMapping("/{id}/publish")
    public RequestDto publish(@PathVariable UUID id) {
        return mapper.map(requestService.publish(id));
    }

    @PostMapping("/{id}/reject")
    public RequestDto reject(@PathVariable UUID id, @Valid @RequestBody ReasonDto request) {
        return mapper.map(requestService.reject(id, request.reason()));
    }

    @PostMapping("/{id}/delete")
    public RequestDto delete(@PathVariable UUID id, @Valid @RequestBody ReasonDto reason) {
        return mapper.map(requestService.delete(id, reason.reason()));
    }
}
