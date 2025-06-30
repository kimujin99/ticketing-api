package com.personal.ticketing_api.service;

import com.personal.ticketing_api.dto.response.QueueEnterResponse;
import com.personal.ticketing_api.dto.response.QueuePositionResponse;
import com.personal.ticketing_api.exception.QueueNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketingService {
    private final StringRedisTemplate redisTemplate;
    private static final String QUEUE_KEY = "ticketingQueue";

    /**
     * 사용자를 대기열에 추가하고 순번 반환
     */
    public QueueEnterResponse enterQueue () {
        // 사용자에게 고유 UUID 부여 // 중복 대기 가능
        String queueToken = UUID.randomUUID().toString();

        // Lua 스크립트: 원자성 보장
        // 1) 토큰을 RPUSH
        // 2) 리스트 사이즈 리턴
        String luaScript = """
            redis.call('RPUSH', KEYS[1], ARGV[1])
            return redis.call('LLEN', KEYS[1])
        """;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        // KEYS[1] = QUEUE_KEY, ARGV[1] = queueToken
        Long position = redisTemplate.execute(redisScript, List.of(QUEUE_KEY), queueToken);

        return new QueueEnterResponse(queueToken, position.intValue());
    }

    /**
     * 대기열 순번 조회 및 입장 가능 여부 확인
     */
    public QueuePositionResponse getQueuePosition(String queueToken) {
        // Lua 스크립트: 원자성 보장
        // 1) 리스트 전체 가져와서 queueToken 위치 찾기
        // 2) 리스트 첫번째 토큰 확인
        // 3) 만약 position == 1 이고 첫번째 토큰이 queueToken 이면 LPOP 하고 enterable=true 리턴
        // 4) 아니면 enterable=false 리턴
        String script = """
        local queueKey = KEYS[1]
        local token = ARGV[1]
        local list = redis.call('LRANGE', queueKey, 0, -1)
        local position = nil
        for i=1,#list do
            if list[i] == token then
                position = i
                break
            end
        end
        if not position then
            return {0, false}
        end
        local firstToken = redis.call('LINDEX', queueKey, 0)
        local enterable = false
        if position == 1 and firstToken == token then
            redis.call('LPOP', queueKey)
            enterable = true
        end
        return {position, enterable}
        """;

        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(List.class);

        List<?> result = redisTemplate.execute(redisScript, List.of(QUEUE_KEY), queueToken);

        if (result == null || result.isEmpty()) {
            throw new QueueNotFoundException("대기열 조회 중 오류가 발생했습니다.");
        }

        if (result.get(0) instanceof Long && ((Long) result.get(0)) == 0L) {
            throw new QueueNotFoundException("해당 대기열 토큰이 존재하지 않습니다.");
        }

        // result[0] = position (Long), result[1] = enterable (Boolean)
        Long positionLong = (Long) result.get(0);
        Long enterableLong = (Long) result.get(1);
        boolean enterable = enterableLong == 1L;

        int position = positionLong.intValue();

        return new QueuePositionResponse(queueToken, position, enterable);
    }

    /**
     * 사용자를 대기열에서 제외하고 결과 반환
     */
    public boolean leaveQueue(String queueToken) {
        Long removedCount = redisTemplate.opsForList().remove(QUEUE_KEY, 0, queueToken);
        if (removedCount == null || removedCount == 0) {
            throw new QueueNotFoundException("해당 대기열 토큰이 존재하지 않습니다.");
        }
        return true;
    }

    /**
     * (디버깅)대기열의 모든 데이터 목록 조회
     * */
    public List<String> getQueueTokens() {
        return redisTemplate.opsForList().range(QUEUE_KEY, 0, -1);
    }
}