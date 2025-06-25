package com.personal.ticketing_api.controller;

import com.personal.ticketing_api.service.RedisTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/redis")
public class RedisTestController {
    private final RedisTestService redisTestService;

    @PostMapping("/set")
    public String setValue(@RequestParam String key, @RequestParam String value) {
        redisTestService.saveValue(key, value);
        return "저장 완료";
    }

    @GetMapping("/get")
    public String getValue(@RequestParam String key) {
        return redisTestService.getValue(key);
    }
}
