package com.marcinlula.requestmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record ReasonDto(@NotBlank String reason) {
}
