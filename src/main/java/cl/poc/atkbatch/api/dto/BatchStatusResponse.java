package cl.poc.atkbatch.api.dto;

import java.time.LocalDateTime;

public record BatchStatusResponse(
        Long jobExecutionId,
        String jobName,
        String status,
        String exitStatus,
        LocalDateTime createTime,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String message) {
}
