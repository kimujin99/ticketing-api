package com.personal.ticketing_api.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@AutoConfigureMockMvc
@SpringBootTest
public class TicketingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        redisTemplate.delete("ticketingQueue");
    }

    @Test
    @DisplayName("Redis 연결 확인 테스트")
    void redisConnectionCheck() {
        String ping = redisTemplate.getConnectionFactory().getConnection().ping();
        System.out.println("Redis Ping Result = " + ping);
    }

    @Test
    @DisplayName("대기열 입장 API 테스트")
    void testEnterQueue() throws Exception {
        mockMvc.perform(post("/ticket/enter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queueToken").exists())
                .andExpect(jsonPath("$.position").exists());

    }

    @Test
    @DisplayName("대기열 조회 API 테스트 - 존재하지 않는 토큰")
    void testGetQueuePosition_NotFount() throws Exception {
        mockMvc.perform(get("/ticket/position/invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("대기열 나가기 API 테스트 - 존재하지 않는 토큰")
    void testLeaveQueuePosition_NotFount() throws Exception {
        mockMvc.perform(delete("/ticket/leave/invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("대기열 순차 입장 후 동시 조회 테스트")
    void testSequentialEnterAndPositionCheck() throws Exception {
        // 순차 작업
        String response1 = mockMvc.perform(post("/ticket/enter"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String queueToken1 = JsonPath.read(response1, "$.queueToken");

        String response2 = mockMvc.perform(post("/ticket/enter"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String queueToken2 = JsonPath.read(response2, "$.queueToken");

        // 동시 작업
        ExecutorService executor = Executors.newFixedThreadPool(2); // 스레드 생성
        CountDownLatch latch = new CountDownLatch(2); // 스레드 대기열

        executor.execute(() -> {
            try {
                mockMvc.perform(get("/ticket/position/" + queueToken1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.queueToken").value(queueToken1))
                        .andExpect(jsonPath("$.position").value(1))
                        .andExpect(jsonPath("$.enterable").value(true));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        executor.execute(() -> {
            try {
                mockMvc.perform(get("/ticket/position/" + queueToken2))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.queueToken").value(queueToken2))
                        .andExpect(jsonPath("$.position").value(1))
                        .andExpect(jsonPath("$.enterable").value(true));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executor.shutdown();
    }

    @Test
    @DisplayName("대기열 동시 입장 후 동시 조회 테스트")
    void testConcurrentEnterAndPositionCheck() throws Exception {
        // 동시 작업
        ExecutorService executor = Executors.newFixedThreadPool(4); // 스레드 생성
        CountDownLatch enterLatch = new CountDownLatch(2); // 스레드 대기열
        CountDownLatch positionLatch = new CountDownLatch(2);

        final String[] tokens = new String[2];

        for(int i = 0; i < 2; i++) {
            final int idx = i;
            executor.execute(() -> {
                try {
                    String response = mockMvc.perform(post("/ticket/enter"))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString();
                    tokens[idx] = JsonPath.read(response, "$.queueToken");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    enterLatch.countDown();
                }
            });
        }

        enterLatch.await();

        for(int i = 0; i < 2; i++) {
            final int idx = i;
            executor.execute(() -> {
                try {
                    mockMvc.perform(get("/ticket/position/" + tokens[idx]))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.queueToken").value(tokens[idx]))
                            .andExpect(jsonPath("$.position").value(1))
                            .andExpect(jsonPath("$.enterable").value(true));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    positionLatch.countDown();
                }
            });
        }

        positionLatch.await();
        executor.shutdown();
    }
}
