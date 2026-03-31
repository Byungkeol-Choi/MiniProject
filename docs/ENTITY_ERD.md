# MiniProject 엔티티 ERD

현재 Java 엔티티(`Member`, `Coupon`) 기준 ERD입니다.

```mermaid
erDiagram
    MEMBER ||--o{ COUPON : has

    MEMBER {
        BIGINT id PK
        VARCHAR phone UK "NOT NULL, length 20"
        VARCHAR name "length 100, nullable"
        INT points "NOT NULL, default 0"
        TIMESTAMP created_at "NOT NULL, updatable=false"
    }

    COUPON {
        BIGINT id PK
        BIGINT member_id FK
        VARCHAR name "NOT NULL, length 100"
        VARCHAR code UK "NOT NULL, length 50"
        VARCHAR discount_type "ENUM(FIXED,PERCENT), NOT NULL"
        INT discount_value "NOT NULL"
        BOOLEAN used "NOT NULL, default false"
        TIMESTAMP expires_at "nullable"
        TIMESTAMP created_at "NOT NULL, updatable=false"
    }
```

---

## 관계 설명

- `Member (1) : Coupon (N)`
- 한 회원은 여러 쿠폰을 가질 수 있습니다.
- 각 쿠폰은 하나의 회원(`member_id`)에 속합니다.

---

## 코드 매핑 참고

- `Member` 엔티티: `src/main/java/com/cafe/kiosk/domain/Member.java`
- `Coupon` 엔티티: `src/main/java/com/cafe/kiosk/domain/Coupon.java`
