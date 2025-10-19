# 1. 설명

이 레포지토리는 Kotlin Spring Boot 에서 red-green-refactor 패턴 기반 TDD 를 학습해보기 위한 레포지토리입니다.

# 2. 기능사항

## API

```
- PATCH `/point/{id}/charge` : 포인트를 충전한다.
- PATCH `/point/{id}/use` : 포인트를 사용한다.
- GET `/point/{id}` : 포인트를 조회한다.
- GET `/point/{id}/histories` : 포인트 내역을 조회한다.
```

## 예외 처리

- 포인트는 0 또는 음수값으로 충전/사용할 수 없음
- 잔고가 부족할 경우, 포인트 사용은 실패하여야 함
