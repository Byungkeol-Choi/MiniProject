# MiniProject 엔드포인트 실행 흐름 (Mermaid)

아래 다이어그램은 현재 Java 코드 기준으로 실제 매핑된 엔드포인트 흐름입니다.

```mermaid
flowchart TD
    A[Client Request] --> B{HTTP Method / Path}

    B -->|GET /| C[MemberController.root]
    C --> D[redirect:/kiosk]
    D --> E[키오스크 메인 페이지 이동]

    B -->|GET /member/login| F[MemberController.stampForm]
    F --> G[Model에 earnedPoints 저장]
    G --> H[return kiosk/stamp]

    B -->|POST /member/stamp| I[MemberController.processStamp]
    I --> J[Model에 earnedPoints 저장]
    J --> K[MemberService.processStamp 호출]

    K --> L[phone 정규화 후 회원 조회]
    L --> M{회원 존재?}
    M -->|No| N[IllegalArgumentException]
    N --> O[Controller catch -> success=false/message]
    O --> P[return kiosk/stamp]

    M -->|Yes| Q[회원 points += earnedPoints]
    Q --> R{points >= 3000?}
    R -->|Yes| S[points -= 3000]
    S --> T[Coupon 생성 및 저장]
    T --> U[name=3,000원 할인 쿠폰]
    U --> V[code=CAFE-XXXXXXXX]
    V --> W[discountType=FIXED]
    W --> X[discountValue=3000]
    X --> Y[expiresAt=now+1month]
    Y --> R
    R -->|No| Z[업데이트된 Member 반환]

    Z --> AA[쿠폰목록 조회 findByMemberIdOrderByCreatedAtDesc]
    AA --> AB[Model: success/memberName/totalPoints/coupons]
    AB --> AC[return kiosk/stamp]

    I --> AD{기타 Exception?}
    AD -->|Yes| AE[error log + 공통 오류메시지]
    AE --> P
```

---

## 보조 흐름: 앱 시작 시 DB 초기화

```mermaid
flowchart TD
    A[Spring Boot Start] --> B[DataInitializer.run]
    B --> C[ALTER TABLE coupon ADD COLUMN IF NOT EXISTS name]
    C --> D[TRUNCATE coupon, member RESTART IDENTITY CASCADE]
    D --> E[샘플 회원 5명 insert]
    E --> F[초기화 완료 로그]
```

---

## 참고

- 현재 코드베이스의 컨트롤러 기준 실제 Java 매핑 엔드포인트:
  - `GET /`
  - `GET /member/login`
  - `POST /member/stamp`
- 템플릿/JS에는 관리자/키오스크 관련 경로가 더 보이지만, Java 컨트롤러 매핑은 별도 파일 존재 여부에 따라 달라질 수 있습니다.
