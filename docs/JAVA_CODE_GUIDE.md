# MiniProject Java 코드 상세 가이드

## 1) 프로젝트 Java 구조 한눈에 보기

현재 `src/main/java/com/cafe/kiosk` 기준으로 코드가 아래 레이어로 구성되어 있습니다.

- `config`: 보안/초기 데이터 설정
- `controller`: HTTP 요청 진입점
- `service`: 비즈니스 로직
- `repository`: DB 접근(JPA)
- `domain`: 엔티티 모델
- `CafeKioskApplication`: 스프링부트 시작점

---

## 2) 파일별 상세 설명

## `CafeKioskApplication`

- 역할: 스프링부트 애플리케이션 부트스트랩
- 핵심: `SpringApplication.run(...)` 으로 컨텍스트 시작
- 비즈니스 로직은 없고 실행 진입점만 담당

## `config/SecurityConfig`

- 역할: Spring Security 전역 보안 정책
- 주요 설정
  - CSRF 토큰 저장소: `CookieCsrfTokenRepository.withHttpOnlyFalse()`
  - 인가 정책:
    - `"/", "/admin/login", "/kiosk"` 는 `permitAll`
    - `"/admin"` 은 `hasRole("ADMIN")`
    - 현재는 `anyRequest().permitAll()` 로 개발 편의 설정
  - 로그인 페이지: `"/admin/login"`
  - 로그인 성공 핸들러: 성공 시 `"/"` 로 리다이렉트
  - 로그아웃 URL: `"/logoutAction"` (세션 무효화 + `JSESSIONID` 삭제)
- 비밀번호 인코더: `BCryptPasswordEncoder(10)`

## `config/DataInitializer`

- 역할: 서버 시작 시 데이터 초기화
- 동작 순서
  1. `coupon` 테이블에 `name` 컬럼이 없으면 추가
     - SQL: `ALTER TABLE coupon ADD COLUMN IF NOT EXISTS name VARCHAR(100) NOT NULL DEFAULT ''`
  2. `coupon`, `member` 테이블 TRUNCATE + 시퀀스 초기화
     - SQL: `TRUNCATE TABLE coupon, member RESTART IDENTITY CASCADE`
  3. 샘플 회원 5명 삽입 (`points = 1000`)
- 의도: 매 실행 시 동일한 테스트 상태 보장

## `controller/MemberController`

- 역할: 회원 포인트 적립 UI 흐름 처리
- 엔드포인트
  - `GET /`:
    - `redirect:/kiosk`
  - `GET /member/login`:
    - 적립 화면(`kiosk/stamp`) 렌더
    - `earnedPoints` 모델 전달
  - `POST /member/stamp`:
    - 입력: `phone`, `earnedPoints`
    - 처리:
      1. `MemberService.processStamp(phone, earnedPoints)` 호출
      2. 성공 시 회원명/누적포인트/쿠폰목록 모델에 담아 `kiosk/stamp` 반환
      3. 예외 시 오류 메시지 모델에 담아 같은 뷰 반환

## `service/MemberService`

- 역할: 회원 조회/포인트 증감/자동 쿠폰 발급
- 주요 메서드
  - `findByPhone(phone)`:
    - 숫자만 남기도록 정규화 후 조회
    - `MemberRepository.findByPhoneNormalized(...)` 사용
  - `addPoints(memberId, points)`:
    - 회원 포인트 증가
  - `deductPoints(memberId, points)`:
    - 포인트 부족 시 `IllegalStateException`
  - `processStamp(phone, earnedPoints)`:
    - 핵심 정책 메서드
    - 회원 없으면 `IllegalArgumentException`
    - 포인트 적립 후, `3000P` 이상일 때 반복적으로 쿠폰 발급
    - 자동 쿠폰 스펙:
      - `name = "3,000원 할인 쿠폰"`
      - `discountType = FIXED`
      - `discountValue = 3000`
      - `used = false`
      - `expiresAt = now + 1 month`
      - `code = CAFE-XXXXXXXX` (UUID 기반)

## `service/CouponService`

- 역할: 쿠폰 검증/할인계산/사용처리/수동발급
- 주요 메서드
  - `validateCoupon(code)`:
    - 미사용 쿠폰인지 확인
    - 만료일(`expiresAt`)이 지나면 예외
  - `calculateDiscount(coupon, originalAmount)`:
    - `FIXED`: 최대 결제금액까지만 할인
    - `PERCENT`: 반올림 퍼센트 계산
  - `useCoupon(couponId)`:
    - `used = true` 처리
  - `issueCoupon(memberId, name, discountType, discountValue, expiresAt)`:
    - 관리자 수동 발급용
    - 쿠폰명(`name`) 포함 발급
  - `generateUniqueCode()`:
    - 중복 없는 랜덤 쿠폰코드 생성

## `domain/Member`

- 테이블: `member`
- 필드
  - `id` (PK)
  - `phone` (unique, not null)
  - `name` (nullable)
  - `points` (not null, 기본 0)
  - `createdAt` (자동 세팅, updatable=false)
- 라이프사이클: `@PrePersist`에서 `createdAt` 자동 주입

## `domain/Coupon`

- 테이블: `coupon`
- 필드
  - `id` (PK)
  - `member` (`ManyToOne`, `member_id`)
  - `name` (not null, length 100)
  - `code` (unique, not null)
  - `discountType` (`FIXED`, `PERCENT`)
  - `discountValue` (not null)
  - `used` (not null, 기본 false)
  - `expiresAt`
  - `createdAt` (자동 세팅)
- 라이프사이클: `@PrePersist`에서 `createdAt` 자동 주입

## `repository/MemberRepository`

- `JpaRepository<Member, Long>` 확장
- 메서드
  - `findByPhone(phone)`
  - `findByPhoneNormalized(phone)`:
    - Native Query로 하이픈 제거 비교
    - 입력 포맷이 `010-xxxx-xxxx` / `010xxxxxxxx` 섞여 있어도 조회 가능

## `repository/CouponRepository`

- `JpaRepository<Coupon, Long>` 확장
- 메서드
  - `findByCodeAndUsedFalse(code)` (미사용 쿠폰 조회)
  - `existsByCode(code)` (코드 중복 확인)
  - `findByMemberIdOrderByCreatedAtDesc(memberId)` (회원 쿠폰 목록)
  - `countByMemberIdAndUsedFalse(memberId)` (회원 미사용 쿠폰 수)
  - `countByUsedFalse()` (전체 미사용 쿠폰 수)

---

## 3) 현재 핵심 비즈니스 정책

- 포인트 적립
  - 주문에서 계산된 `earnedPoints` 만큼 회원 포인트 적립
- 쿠폰 자동 발급
  - 적립 후 포인트가 `3000` 이상이면 `3000` 차감 + 쿠폰 1장 발급
  - 조건 충족 시 반복 발급(예: 6500P이면 2장 발급 후 500P 잔여)
- 쿠폰 이름 정책
  - 자동 발급: `"3,000원 할인 쿠폰"`
  - 관리자 수동 발급: 폼 입력 이름 사용

---

## 4) 트랜잭션 경계

- 클래스 기본: `@Transactional(readOnly = true)` (Service 계층)
- 쓰기 작업 메서드만 `@Transactional` 재선언
  - `MemberService.addPoints`, `deductPoints`, `processStamp`
  - `CouponService.useCoupon`, `issueCoupon`
  - `DataInitializer.run`

이 구조 덕분에 조회/변경 의도가 명확하고, 변경 메서드의 원자성도 확보됩니다.
