package com.marcinlula.requestmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBodyDto(@NotBlank String body) {
}
