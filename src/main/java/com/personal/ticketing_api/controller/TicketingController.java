package com.personal.ticketing_api.controller;

import com.personal.ticketing_api.dto.response.QueueEnterResponse;
import com.personal.ticketing_api.dto.response.QueuePositionResponse;
import com.personal.ticketing_api.service.TicketingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ticket")
@Tag(name = "티켓팅 대기열 API", description = "티켓팅 대기열 관련 API")
public class TicketingController {
    private final TicketingService ticketingService;

    @Operation(summary = "대기열 입장 (UUID 발급 및 등록)", description = "사용자를 대기열에 등록하고 고유 UUID와 현재 대기 순번을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대기열 입장 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러 발생")
    })
    @PostMapping("/enter")
    public ResponseEntity<QueueEnterResponse> enterQueue() {
        QueueEnterResponse response = ticketingService.enterQueue();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "대기 순번 조회 및 입장 가능 여부 확인", description = "현재 자신의 대기 순번과 입장 가능 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "대기열 토큰 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/position/{queueToken}")
    public ResponseEntity<QueuePositionResponse> getQueuePosition(
            @PathVariable String queueToken) {

        QueuePositionResponse response = ticketingService.getQueuePosition(queueToken);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "대기열 나가기 (UUID 삭제)", description = "사용자를 대기열에서 제외합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "대기열 토큰 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/leave/{queueToken}")
    public ResponseEntity<Boolean> leaveQueue(
            @PathVariable String queueToken) {

        boolean result = ticketingService.leaveQueue(queueToken);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "(디버깅)대기열의 모든 데이터 목록 조회", description = "현재 대기열에 들어있는 모든 토큰 값을 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/listAll")
    public ResponseEntity<List<String>> getQueueTokens() {
        List<String> result = ticketingService.getQueueTokens();
        return ResponseEntity.ok(result);
    }
}