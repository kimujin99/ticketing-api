package com.personal.ticketing_api.controller;

import com.personal.ticketing_api.service.RedisTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/redis")
@Tag(name = "Redis 테스트 API", description = "Redis 연결 테스트 관련 API")
public class RedisTestController {
    private final RedisTestService redisTestService;

    @Operation(summary = "테스트 데이터 저장", description = "Redis 서버에 데이터를 저장합니다.")
    @PostMapping("/set")
    public String setValue(@RequestParam String key, @RequestParam String value) {
        redisTestService.saveValue(key, value);
        return "저장 완료";
    }

    @Operation(summary = "테스트 데이터 조회", description = "Redis 서버에서 데이터를 조회합니다.")
    @GetMapping("/get")
    public String getValue(@RequestParam String key) {
        return redisTestService.getValue(key);
    }

    @Operation(summary = "테스트 데이터 삭제", description = "Redis 서버에서 데이터를 삭제합니다.")
    @PostMapping("/delete")
    public String deleteValue(@RequestParam String key) {
        return redisTestService.deleteValue(key);
    }
}
