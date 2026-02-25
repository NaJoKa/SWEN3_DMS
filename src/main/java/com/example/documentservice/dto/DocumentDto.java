package com.example.documentservice.dto;

import jakarta.validation.constraints.Size;

public record DocumentDto(Long id,
                          @Size(max = 255) String name,
                          @Size(max = 255) String objectKey,
                          Long ownerId) {
}
