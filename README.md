# 🎟️ Ticketing Queue API

Redis 기반 대기열 시스템으로, 사용자 순번 조회 및 입장, 퇴장 기능을 제공하는 RESTful API 프로젝트입니다.

---

## 📖 목차

- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [API 명세](#api-명세)
- [프로젝트 구조](#프로젝트-구조)
- [트러블 슈팅](#트러블-슈팅)
- [기타 정보](#기타-정보)

---

## 프로젝트 개요

- **목적**: 간단한 대기열 관리 서비스 구현  
- **특징**
  - Redis List를 이용한 대기열 구현
  - Lua 스크립트를 이용해 원자적(atomic) 처리 보장
  - @RestControllerAdvice 기반 전역 예외 처리
  - JUnit + MockMvc 기반의 테스트 자동화
- **프로세스**
<img src="https://github.com/user-attachments/assets/f5ad44e2-b52e-4e72-8881-1587006112d8" width="500"/>

---

## 기술 스택

| 구분       | 기술                          |
|:-----------|:--------------------------------|
| Language   | Java 21                        |
| Framework  | Spring Boot 3.2.6               |
| Database   | Redis 3.0.504                    |
| Build Tool | Gradle                          |
| Test       | JUnit 5, MockMvc                |
| Docs       | Springdoc OpenAPI (Swagger UI) 2.5.0 |

---

## API 명세

| Method | URL                            | 설명                     | 요청값(Request)                 | 응답값(Response)                       | 응답코드                |
|:--------|:-----------------------------|:-------------------------|:--------------------------------|:----------------------------------|:------------------|
| `POST`  | `/ticket/enter`               | 대기열 입장 (UUID 발급 및 등록) | 없음                           | `queueToken`, `position`             | `200`, `500`        |
| `GET`   | `/ticket/position/{queueToken}` | 대기 순번 조회 및 입장 가능 여부 확인 | `queueToken` (Path Variable) | `position`, `enterable`             | `200`, `404`, `500` |
| `DELETE`| `/ticket/leave/{queueToken}`  | 대기열 나가기 (UUID 삭제)      | `queueToken` (Path Variable) | `message`                           | `200`, `404`, `500` |
| `GET`   | `/ticket/listAll`             | (디버깅) 현재 대기열 목록 조회     | 없음                           | `List<String>`                     | `200`               |

---

## 프로젝트 구조
```
src/main/java/com/personal/ticketing_api/
├── controller/
├── service/
├── dto/
├── exception/
├── config/
└── TicketingApiApplication.java
src/test/java/com/personal/ticketing_api/
└── controller/
```

---

## 트러블 슈팅
### 📌 글로벌 예외 처리 클래스 생성 후, Swagger 오류 발생

- **상황**  
  <img src="https://github.com/user-attachments/assets/499e561b-74f6-4a89-9883-5e2db21c884b" width="500" />

- **발생 로그**
  ```
  Resolved [jakarta.servlet.ServletException: Handler dispatch failed: java.lang.NoSuchMethodError: 'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)']
  ```

- **원인**  
`@RestControllerAdvice` 추가 후 발생.  
Spring Boot 버전과 Swagger 라이브러리 버전이 맞지 않아 jakarta.servlet 충돌로 발생.

- **해결**  
Spring Boot 버전을 '3.2.6'으로 하향하여 해결.  
(기존)  
`'org.springframework.boot' version '3.5.3'`  
`implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'`

<br>

### 📌 동시성 테스트 진행중, 각 스레드에서 500에러 발생

- **상황**  
MockMvc 기반 동시성 테스트 진행 중, 각 스레드에서 Status expected:<200> but was:<500> 에러 발생.

- **발생 로그**
  ```
  Exception in thread "pool-3-thread-2" java.lang.AssertionError: Status expected:<200> but was:<500>
  	at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:59)
  	at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:122)
  	at org.springframework.test.web.servlet.result.StatusResultMatchers.lambda$matcher$9(StatusResultMatchers.java:637)
  	at org.springframework.test.web.servlet.MockMvc$1.andExpect(MockMvc.java:214)
  	at com.personal.ticketing_api.controller.TicketingControllerTest.lambda$testSequentialEnterAndPositionCheck$1(TicketingControllerTest.java:101)
  	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
  	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
  	at java.base/java.lang.Thread.run(Thread.java:1583)
  Exception in thread "pool-3-thread-1" java.lang.AssertionError: Status expected:<200> but was:<500>
  	at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:59)
  	at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:122)
  	at org.springframework.test.web.servlet.result.StatusResultMatchers.lambda$matcher$9(StatusResultMatchers.java:637)
  	at org.springframework.test.web.servlet.MockMvc$1.andExpect(MockMvc.java:214)
  	at com.personal.ticketing_api.controller.TicketingControllerTest.lambda$testSequentialEnterAndPositionCheck$0(TicketingControllerTest.java:87)
  	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
  	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
  	at java.base/java.lang.Thread.run(Thread.java:1583)
  ```

- **원인**  
  ```java
  List<?> result = redisTemplate.execute(redisScript, List.of(QUEUE_KEY), queueToken);
  
  Long positionLong = (Long) result.get(0);
  Boolean enterable = (Boolean) result.get(1);
  ```
  Lua의 boolean이 자바에선 Long(1 또는 0)으로 매핑되어 `InvocationTargetException` 발생

- **해결**  
  ```java
	Long enterableLong = (Long) result.get(1);
	boolean enterable = enterableLong == 1L;
  ```
  Long으로 받아 boolean으로 캐스팅하여 해결
  
---

## 기타 정보
- **Velog** : https://velog.io/@kimujin99/series/Spring-Redis-%ED%8B%B0%EC%BC%93%ED%8C%85-%EB%8C%80%EA%B8%B0%EC%97%B4-API
