package com.marcinlula.requestmanagement.dto;

import com.marcinlula.requestmanagement.model.RequestState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RequestDto(
        @NotNull
        UUID id,
        @NotBlank @Size(max = 255)
        String name,
        @NotBlank
        String body,
        @NotNull
        RequestState state,
        String reason,
        @Positive
        Long publicationNumber
) {
}
