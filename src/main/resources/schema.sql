-- =====================================================
-- 브런치 카페 키오스크 - Supabase(PostgreSQL) DDL
-- Supabase SQL Editor에서 순서대로 실행하세요.
-- =====================================================

-- 회원 테이블
CREATE TABLE IF NOT EXISTS member (
                                      id         BIGSERIAL PRIMARY KEY,
                                      phone      VARCHAR(20)  NOT NULL UNIQUE,
    name       VARCHAR(100),
    points     INT          NOT NULL DEFAULT 0,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 메뉴 테이블
CREATE TABLE IF NOT EXISTS menu (
                                    id          BIGSERIAL PRIMARY KEY,
                                    name        VARCHAR(100) NOT NULL,
    price       INT          NOT NULL,
    category    VARCHAR(10)  NOT NULL CHECK (category IN ('FOOD', 'DRINK')),
    image_url   VARCHAR(500),
    description VARCHAR(500),
    available   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 주문 테이블
CREATE TABLE IF NOT EXISTS orders (
    id              BIGSERIAL PRIMARY KEY,
    member_id       BIGINT       REFERENCES member(id),
    total_amount    INT          NOT NULL,
    discount_amount INT          NOT NULL DEFAULT 0,
    payment_method  VARCHAR(20),
    status          VARCHAR(20)  NOT NULL DEFAULT 'RECEIVED'
    CHECK (status IN ('RECEIVED', 'PREPARING', 'COMPLETED', 'CANCELLED')),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 주문 상품 테이블
CREATE TABLE IF NOT EXISTS order_item (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_id    BIGINT NOT NULL REFERENCES menu(id),
    quantity   INT    NOT NULL,
    unit_price INT    NOT NULL
    );

-- 쿠폰 테이블
CREATE TABLE IF NOT EXISTS coupon (
                                      id             BIGSERIAL PRIMARY KEY,
                                      member_id      BIGINT       REFERENCES member(id),
    code           VARCHAR(50)  NOT NULL UNIQUE,
    discount_type  VARCHAR(10)  NOT NULL CHECK (discount_type IN ('FIXED', 'PERCENT')),
    discount_value INT          NOT NULL,
    used           BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at     TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 관리자 테이블
CREATE TABLE IF NOT EXISTS admin (
                                     id         BIGSERIAL PRIMARY KEY,
                                     username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- =====================================================
-- 샘플 메뉴 데이터
-- =====================================================
INSERT INTO menu (name, price, category, description, available) VALUES
        ('에그 베네딕트',    16500, 'FOOD',  '홀란다이즈 소스와 수란이 올라간 브런치의 정석', TRUE),
        ('아보카도 토스트',  13500, 'FOOD',  '신선한 아보카도와 크림치즈를 올린 사워도우 토스트', TRUE),
        ('팬케이크 스택',    12000, 'FOOD',  '메이플 시럽과 신선한 과일이 함께 나오는 팬케이크', TRUE),
        ('샥슈카',           14000, 'FOOD',  '토마토 소스에 포치드 에그가 올라간 중동식 브런치', TRUE),
        ('크로크 무슈',      13000, 'FOOD',  '햄과 그뤼에르 치즈를 넣은 프렌치 토스트 샌드위치', TRUE),
        ('프렌치 어니언 수프', 10000, 'FOOD', '캐러멜라이즈드 양파와 치즈 크루통이 올라간 수프', TRUE),
        ('아메리카노',        5000, 'DRINK', '진하고 깔끔한 에스프레소 베이스 커피', TRUE),
        ('카페 라떼',         5500, 'DRINK', '부드러운 스팀 밀크와 에스프레소의 조화', TRUE),
        ('플랫 화이트',       6000, 'DRINK', '진한 에스프레소에 마이크로폼 밀크를 더한 커피', TRUE),
        ('카푸치노',          5500, 'DRINK', '에스프레소와 스팀 밀크, 풍부한 폼이 조화로운 커피', TRUE),
        ('자몽 에이드',       6500, 'DRINK', '신선한 자몽으로 만든 상큼한 에이드', TRUE),
        ('딸기 스무디',       7000, 'DRINK', '신선한 딸기로 만든 진한 스무디', TRUE)
    ON CONFLICT DO NOTHING;

-- 샘플 관리자 계정 (비밀번호: admin1234 - BCrypt 해시)
INSERT INTO admin (username, password) VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy')
    ON CONFLICT DO NOTHING;

-- 임의 회원 추가
INSERT INTO member (phone, name, points, created_at)
VALUES ('010-1234-5678', '홍길동', 0, CURRENT_TIMESTAMP);