package com.marcinlula.requestmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBodyDto(@NotBlank String body) {
}
