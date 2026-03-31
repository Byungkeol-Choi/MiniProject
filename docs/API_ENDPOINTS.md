# 브런치 카페 키오스크 API 엔드포인트 정리

Base URL: `http://localhost:8080`

관련 상세 문서:
- Java 코드 상세 설명: `JAVA_CODE_GUIDE.md`
- 실행 흐름 다이어그램(Mermaid): `ENDPOINT_FLOW_MERMAID.md`

---

## 1) 엔드포인트 목록 (현재 Java 컨트롤러 기준)

## `GET /`

- Controller: `MemberController.root()`
- 동작: 루트 요청을 키오스크 메인으로 리다이렉트
- 응답: `redirect:/kiosk`

## `GET /member/login`

- Controller: `MemberController.stampForm(...)`
- 목적: 포인트 적립 화면 진입
- Query Parameters:
  - `earnedPoints` (optional, default `0`)
- View: `kiosk/stamp`

## `POST /member/stamp`

- Controller: `MemberController.processStamp(...)`
- 목적: 전화번호 회원 조회 후 포인트 적립 + 자동 쿠폰 발급
- Form Parameters:
  - `phone` (required)
  - `earnedPoints` (optional, default `0`)
- 내부 호출:
  - `MemberService.processStamp(phone, earnedPoints)`
  - 성공 후 `CouponRepository.findByMemberIdOrderByCreatedAtDesc(memberId)`로 쿠폰 목록 조회
- View: `kiosk/stamp`

---

## 2) 엔드포인트별 처리 결과

## `POST /member/stamp` 성공 케이스

- `success = true`
- `memberName` (회원 이름 없으면 전화번호 대체)
- `totalPoints` (쿠폰 발급 차감 반영 후 잔여 포인트)
- `coupons` (회원 쿠폰 최신순 목록)

## `POST /member/stamp` 실패 케이스

- 회원 미존재 등 비즈니스 오류:
  - `success = false`
  - `message = 예외 메시지`
- 기타 시스템 오류:
  - 로그 기록 후
  - `success = false`
  - `message = "포인트 적립 중 오류가 발생했습니다."`

---

## 3) 적립/쿠폰 정책

- 적립: 입력된 `earnedPoints` 만큼 포인트 증가
- 자동 쿠폰 발급 기준:
  - 누적 포인트가 `3000` 이상인 동안 반복 발급
  - 발급 시마다 `3000` 포인트 차감
- 자동 발급 쿠폰 스펙:
  - `name = "3,000원 할인 쿠폰"`
  - `discountType = FIXED`
  - `discountValue = 3000`
  - `used = false`
  - `expiresAt = now + 1 month`
  - `code = CAFE-XXXXXXXX` (UUID 기반)

---

## 4) DB 초기화/스키마 보정 정책

- 서버 시작 시 `DataInitializer.run()` 실행
- Supabase pooler 환경 대응:
  - `ALTER TABLE coupon ADD COLUMN IF NOT EXISTS name VARCHAR(100) NOT NULL DEFAULT ''`
- 이후 초기화:
  - `TRUNCATE TABLE coupon, member RESTART IDENTITY CASCADE`
  - 샘플 회원 5명 삽입

---

## 5) 보안 관련 참고

- `SecurityConfig` 기준:
  - `"/", "/admin/login", "/kiosk"`: `permitAll`
  - `"/admin"`: `hasRole("ADMIN")`
  - 현재 개발 편의상 `anyRequest().permitAll()` 상태
- CSRF는 Cookie 기반 토큰 저장소 사용

---

## 6) Mermaid 흐름도 안내

엔드포인트 실행 순서 및 분기 흐름은 아래 문서에서 확인:

- `ENDPOINT_FLOW_MERMAID.md`
