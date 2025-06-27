package com.personal.ticketing_api.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "대기 등록 응답 모델")
public class QueueEnterResponse {
    @Schema(description = "사용자의 대기 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String queueToken;

    @Schema(description = "사용자의 대기 순번", example = "122")
    private int position;
}
