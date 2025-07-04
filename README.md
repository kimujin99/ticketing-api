# 🎟️ Ticketing Queue API

Redis 기반 대기열 시스템으로, 사용자 순번 조회 및 입장, 퇴장 기능을 제공하는 RESTful API 프로젝트입니다.

---

## 📖 목차

- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [API 명세](#api-명세)
- [프로젝트 구조](#프로젝트-구조)

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
