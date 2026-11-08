package com.marcinlula.requestmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ReasonDto(@NotBlank String reason) {
}
