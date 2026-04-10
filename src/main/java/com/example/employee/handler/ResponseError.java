package com.example.employee.handler;

import lombok.Builder;
import java.time.Instant;

@Builder
public record ResponseError(
        String message,
        String field,
        String path,
        String errorCode,
        Instant timestamp
) {
}
