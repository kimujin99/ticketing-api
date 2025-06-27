package com.personal.ticketing_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "대기 조회 응답 모델")
public class QueuePositionResponse {
    @Schema(description = "사용자의 대기 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String queueToken;

    @Schema(description = "사용자의 대기 순번", example = "122")
    private int position;

    @Schema(description = "입장 가능 여부", example = "true")
    private boolean enterable;
}