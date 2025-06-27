package com.personal.ticketing_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "API 에러 응답 모델")
public class ErrorResponse {
    @Schema(description = "HTTP 상태 코드", example = "404")
    private int status;

    @Schema(description = "에러 코드", example = "QUEUE_NOT_FOUND")
    private String errorCode;

    @Schema(description = "에러 메시지", example = "해당 대기열 토큰이 없습니다.")
    private String message;

    @Schema(description = "요청 경로", example = "/ticket/position/123e4567-e89b-12d3-a456-426614174000")
    private String path;

    @Schema(description = "발생 시각", example = "2025-06-26T13:40:00")
    private LocalDateTime timestamp;
}
