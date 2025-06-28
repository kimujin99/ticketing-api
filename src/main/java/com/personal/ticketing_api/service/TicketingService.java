package com.personal.ticketing_api.service;

import com.personal.ticketing_api.dto.response.QueueEnterResponse;
import com.personal.ticketing_api.dto.response.QueuePositionResponse;
import com.personal.ticketing_api.exception.QueueNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
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

        redisTemplate.opsForList().rightPush(QUEUE_KEY, queueToken);
        Long size = redisTemplate.opsForList().size(QUEUE_KEY);

        int position = size.intValue();

        return new QueueEnterResponse(queueToken, position);
    }

    /**
     * 대기열 순번 조회 및 입장 가능 여부 확인
     */
    public QueuePositionResponse getQueuePosition(String queueToken) {
        List<String> queueList = redisTemplate.opsForList().range(QUEUE_KEY, 0, -1);

        if (queueList == null || !queueList.contains(queueToken)) {
            throw new QueueNotFoundException("해당 대기열 토큰이 존재하지 않습니다.");
        }

        // 현재 순번 구하기 (index + 1)
        int position = queueList.indexOf(queueToken) + 1;
        String tokenAtFirst = redisTemplate.opsForList().index(QUEUE_KEY, 0);
        boolean enterable = false;

        if (position == 1 && tokenAtFirst.equals(queueToken)) {
            redisTemplate.opsForList().leftPop(QUEUE_KEY);
            enterable = true;
        }

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