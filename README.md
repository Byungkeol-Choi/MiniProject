# Mini Project — 브런치 카페 키오스크

Java 21 · Thymeleaf · JavaScript · Spring Security · PostgreSQL(Supabase)

발표 자료(로컬 클론): `docs/MiniProject-presentation-V1.0.pptx` — GitHub에는 `docs/`가 기본 무시될 수 있으니 팀 내 별도 공유를 활용하세요.

## 프로젝트 개요

터치 키오스크 환경을 가정한 **브런치 카페 주문 시스템**입니다. 고객은 메뉴 선택 → 장바구니 → 쿠폰·결제까지 한 흐름으로 주문하고, **관리자**는 동일 DB 기준으로 조리·픽업 상태, 메뉴·회원·쿠폰을 관리합니다.

| 구분 | 설명 |
|------|------|
| **키오스크** | 음식/음료 탭, 메뉴·가격·이미지, 장바구니, 주문 확인, 결제, 포인트 적립(스탬프) |
| **관리자** | 대시보드(매출·주문 현황 요약), 주문 관리(상태: 접수·준비중·완료·취소), 회원·쿠폰, 메뉴 CRUD·품절 |

## 기술 스택

- **런타임 / 프레임워크**: Java 21, Spring Boot 4.0.3 (Web, Thymeleaf, Data JPA, Security)
- **빌드**: Gradle
- **보안**: Spring Security, CSRF, `ROLE_ADMIN`
- **프론트**: Thymeleaf, Vanilla JS, `kiosk.css` / `admin.css`
- **DB**: Supabase(PostgreSQL), JPA — 스키마는 `src/main/resources/schema.sql` 참고

## 실행 방법

1. **JDK 21** 설치
2. `src/main/resources/application.yml`에 Supabase JDBC URL·계정 정보 설정  
   (저장소에 비밀번호를 올리지 마세요. 로컬 전용 설정 또는 환경 변수 사용을 권장합니다.)
3. Supabase SQL Editor에서 `schema.sql` 실행
4. 프로젝트 루트에서:

```bash
./gradlew bootRun
```

- 기본 포트: **8080**
- 키오스크: `http://localhost:8080/`
- 관리자 로그인: `http://localhost:8080/admin/login`

## 주요 URL

### 키오스크 (Main flow)

| 경로 | 화면 |
|------|------|
| `GET /` | 메인 메뉴 (`index.html`) |
| 주문 흐름 | `cart.html` → `payment.html` → `complete.html` |
| `GET /member/stamp` | 포인트 적립 (`stamp.html`) |

장바구니·주문은 세션 및 `kiosk.js` / 컨트롤러·API와 연동됩니다.

### 관리자 (`ROLE_ADMIN`)

| 경로 | 설명 |
|------|------|
| `/admin/login` | 로그인 |
| `/admin/dashboard` | 대시보드 |
| `/admin/menus` | 메뉴 목록·등록·수정 |
| `/admin/orders` | 주문 목록·상태·상세 |
| `/admin/members` | 회원·쿠폰 조회 등 |

REST API는 `/admin/api/*` 등 프로젝트 구현을 참고하세요.

## 데이터베이스 (요약)

| 테이블 | 용도 |
|--------|------|
| `menu` | 메뉴명, 가격, `FOOD`/`DRINK`, 이미지 URL, 설명, 판매 여부 |
| `admin` | 관리자 계정(BCrypt 비밀번호) |
| `orders` | 회원 FK, 총액, 할인, 결제수단, 상태, 생성 시각 |
| `order_item` | 주문·메뉴 FK, 수량, 단가 |
| `member` | 전화번호(고유), 이름, 포인트 |
| `coupon` | 회원 FK, 코드, 정액/정률, 사용 여부, 만료 |

상세 컬럼은 `schema.sql` 및 발표 슬라이드 **Database** 파트를 참고하세요.

## 프로젝트 구조 (요약)

```
src/main/java/com/cafe/kiosk/
├── CafeKioskApplication.java
├── config/          # SecurityConfig, DataInitializer 등
├── controller/      # Kiosk, Order, Member, Admin (+ API 컨트롤러)
├── domain/          # JPA 엔티티
├── dto/
├── repository/
├── service/
└── security/        # 예: AdminUserDetailsService

src/main/resources/
├── application.yml
├── schema.sql
├── static/          # css, js, images
└── templates/
    ├── fragments/layout.html
    ├── kiosk/       # index, cart, payment, complete, stamp
    └── admin/       # login, dashboard, menus, orders, members
```

## 팀 업무 분장 (발표 기준)

- **메뉴**: FOOD/DRINK, 가격, 이미지 URL, 관리자 CRUD, 키오스크 메인 UI
- **주문·결제**: 세션 장바구니, 쿠폰, 결제 수단, 주문 상태, `cart` / `payment` / `complete` 화면
- **회원·쿠폰**: 스탬프(적립), 쿠폰 정액/정률, 관리자 회원·쿠폰 화면
- **팀장(리드)**: Gradle·패키지 기반, Git PR/머지, `schema.sql`, Spring Security 경로·CSRF, 통합

## Git 브랜치 전략 (발표 요약)

- **`main`**: 안정 버전
- **`dev`**: 일상 통합 브랜치 (`origin/HEAD` 연동 등 팀 규칙에 맞게 사용)
- 기능 브랜치 예: `menu`, `order`, `member`, `admin` 등 — 완료 후 `dev`로 머지

브랜치 이름·버전 규칙은 팀 내 합의(예: `기능명` + 버전)를 따릅니다.

## 이슈 · 문서

- **GitHub Issues**: [MiniProject Issues](https://github.com/Byungkeol-Choi/MiniProject/issues)
- 팀 진행·회의 사항 등은 발표 슬라이드 및 공용 스프레드시트(발표 자료 링크)를 참고하세요.

## 라이선스

팀·교육 과제용 미니 프로젝트입니다. 별도 라이선스가 없으면 내부 규정을 따릅니다.

---

*본 README는 `docs/MiniProject-presentation-V1.0.pptx` (V1.0) 내용을 바탕으로 정리되었습니다.*
