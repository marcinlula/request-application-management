package com.marcinlula.requestmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRequestDto(
        @NotBlank @Size(max = 255)
        String name,
        @NotBlank
        String body) {
}
