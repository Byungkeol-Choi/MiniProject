-- =====================================================
-- 브런치 카페 키오스크 - Supabase(PostgreSQL) DDL
-- Supabase SQL Editor에서 순서대로 실행하세요.
-- =====================================================

-- 회원 테이블
CREATE TABLE IF NOT EXISTS member
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    phone
    VARCHAR
(
    20
) NOT NULL UNIQUE,
    name VARCHAR
(
    100
),
    points INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 메뉴 테이블
CREATE TABLE IF NOT EXISTS menu
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    100
) NOT NULL,
    price INT NOT NULL,
    category VARCHAR
(
    10
) NOT NULL CHECK
(
    category
    IN
(
    'FOOD',
    'DRINK'
)),
    image_url VARCHAR
(
    500
),
    description VARCHAR
(
    500
),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 주문 테이블
CREATE TABLE IF NOT EXISTS orders
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    member_id
    BIGINT
    REFERENCES
    member
(
    id
),
    total_amount INT NOT NULL,
    discount_amount INT NOT NULL DEFAULT 0,
    payment_method VARCHAR
(
    20
),
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'RECEIVED'
    CHECK
(
    status
    IN
(
    'RECEIVED',
    'PREPARING',
    'COMPLETED',
    'CANCELLED'
)),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 주문 상품 테이블
CREATE TABLE IF NOT EXISTS order_item
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    order_id
    BIGINT
    NOT
    NULL
    REFERENCES
    orders
(
    id
) ON DELETE CASCADE,
    menu_id BIGINT NOT NULL REFERENCES menu
(
    id
),
    quantity INT NOT NULL,
    unit_price INT NOT NULL
    );

-- 쿠폰 테이블
CREATE TABLE IF NOT EXISTS coupon
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    member_id
    BIGINT
    REFERENCES
    member
(
    id
),
    code VARCHAR
(
    50
) NOT NULL UNIQUE,
    discount_type VARCHAR
(
    10
) NOT NULL CHECK
(
    discount_type
    IN
(
    'FIXED',
    'PERCENT'
)),
    discount_value INT NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 관리자 테이블
CREATE TABLE IF NOT EXISTS admin
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    username
    VARCHAR
(
    50
) NOT NULL UNIQUE,
    password VARCHAR
(
    255
) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- =====================================================
-- 샘플 메뉴 데이터
-- =====================================================
INSERT INTO menu (name, price, category, image_url, description, available)
VALUES ('에그 베네딕트', 16500, 'FOOD', '/images/menu/eggs-benedict.png', '홀란다이즈 소스와 수란이 올라간 브런치의 정석', TRUE),
       ('아보카도 토스트', 13500, 'FOOD', '/images/menu/abocado-toast.png', '신선한 아보카도와 크림치즈를 올린 사워도우 토스트', TRUE),
       ('팬케이크 스택', 12000, 'FOOD', '/images/menu/pancake.png', '메이플 시럽과 신선한 과일이 함께 나오는 팬케이크', TRUE),
       ('샥슈카', 14000, 'FOOD', '', '토마토 소스에 포치드 에그가 올라간 중동식 브런치', TRUE),
       ('크로크 무슈', 13000, 'FOOD', '/images/menu/croque-monsieur.png', '햄과 그뤼에르 치즈를 넣은 프렌치 토스트 샌드위치', TRUE),
       ('프렌치 어니언 수프', 10000, 'FOOD', '/images/menu/onion-soup.png', '캐러멜라이즈드 양파와 치즈 크루통이 올라간 수프', TRUE),
       ('Hot아메리카노', 5000, 'DRINK', '/images/menu/hot-americano.png', '진하고 깔끔한 에스프레소 베이스 커피', TRUE),
       ('Ice아메리카노', 5000, 'DRINK', '/images/menu/ice-americano.png', '진하고 깔끔한 에스프레소 베이스 커피', TRUE),
       ('Hot카페 라떼', 5500, 'DRINK', '/images/menu/hot-cafelatte.png', '부드러운 스팀 밀크와 에스프레소의 조화', TRUE),
       ('Ice카페 라떼', 5500, 'DRINK', '/images/menu/ice-cafelatte.png', '부드러운 스팀 밀크와 에스프레소의 조화', TRUE),
       ('플랫 화이트', 6000, 'DRINK', '', '진한 에스프레소에 마이크로폼 밀크를 더한 커피', TRUE),
       ('카푸치노', 5500, 'DRINK', '', '에스프레소와 스팀 밀크, 풍부한 폼이 조화로운 커피', TRUE),
       ('자몽 에이드', 6500, 'DRINK', '', '신선한 자몽으로 만든 상큼한 에이드', TRUE),
       ('딸기 스무디', 7000, 'DRINK', '', '신선한 딸기로 만든 진한 스무디', TRUE) ON CONFLICT DO NOTHING;

-- 샘플 관리자 계정 (비밀번호: admin1234 - BCrypt 해시)
INSERT INTO admin (username, password)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy') ON CONFLICT DO NOTHING;

-- 임의 회원 추가
INSERT INTO member (phone, name, points, created_at)
VALUES ('010-1234-5678', '홍길동', 0, CURRENT_TIMESTAMP);

-- admin/dashboard 페이지에 보여줄 order, order_item 샘플 데이터
INSERT INTO orders (member_id, total_amount, discount_amount, payment_method, status, created_at)
VALUES ((SELECT id FROM member ORDER BY id LIMIT 1), 21500, 0, 'CARD', 'RECEIVED', NOW() - INTERVAL '2 hours'
    );
INSERT INTO order_item (order_id, menu_id, quantity, unit_price)
VALUES (currval(pg_get_serial_sequence('orders', 'id')),
        (SELECT id FROM menu WHERE name = '에그 베네딕트' LIMIT 1), 1, 16500),
    (currval(pg_get_serial_sequence('orders', 'id')),
     (SELECT id FROM menu WHERE name = '아메리카노' LIMIT 1), 1, 5000);
-- 주문 2: PREPARING — 아보카도 토스트 1 + 카페 라떼 1, 할인 500원 = 18,500
INSERT INTO orders (member_id, total_amount, discount_amount, payment_method, status, created_at)
VALUES ((SELECT id FROM member ORDER BY id LIMIT 1), 18500, 500, 'CARD', 'PREPARING', NOW() - INTERVAL '45 minutes'
    );
INSERT INTO order_item (order_id, menu_id, quantity, unit_price)
VALUES (currval(pg_get_serial_sequence('orders', 'id')),
        (SELECT id FROM menu WHERE name = '아보카도 토스트' LIMIT 1), 1, 13500),
    (currval(pg_get_serial_sequence('orders', 'id')),
     (SELECT id FROM menu WHERE name = '카페 라떼' LIMIT 1), 1, 5500);
-- 주문 3: COMPLETED — 비회원, 팬케이크 1 + 자몽 에이드 1 = 18,500
INSERT INTO orders (member_id, total_amount, discount_amount, payment_method, status, created_at)
VALUES (NULL,
        18500,
        0,
        'CASH',
        'COMPLETED',
        NOW() - INTERVAL '1 day');
INSERT INTO order_item (order_id, menu_id, quantity, unit_price)
VALUES (currval(pg_get_serial_sequence('orders', 'id')),
        (SELECT id FROM menu WHERE name = '팬케이크 스택' LIMIT 1), 1, 12000),
    (currval(pg_get_serial_sequence('orders', 'id')),
     (SELECT id FROM menu WHERE name = '자몽 에이드' LIMIT 1), 1, 6500);
-- 주문 4: RECEIVED — 아메리카노 3잔 = 15,000
INSERT INTO orders (member_id, total_amount, discount_amount, payment_method, status, created_at)
VALUES ((SELECT id FROM member ORDER BY id LIMIT 1), 15000, 0, 'EASY_PAY', 'RECEIVED', NOW() - INTERVAL '10 minutes'
    );
INSERT INTO order_item (order_id, menu_id, quantity, unit_price)
VALUES (currval(pg_get_serial_sequence('orders', 'id')),
        (SELECT id FROM menu WHERE name = '아메리카노' LIMIT 1), 3, 5000);
-- 주문 5: CANCELLED — 크로크 무슈 1건만 기록 (총액 13,000)
INSERT INTO orders (member_id, total_amount, discount_amount, payment_method, status, created_at)
VALUES ((SELECT id FROM member ORDER BY id LIMIT 1), 13000, 0, 'CARD', 'CANCELLED', NOW() - INTERVAL '3 hours'
    );
INSERT INTO order_item (order_id, menu_id, quantity, unit_price)
VALUES (currval(pg_get_serial_sequence('orders', 'id')),
        (SELECT id FROM menu WHERE name = '크로크 무슈' LIMIT 1), 1, 13000);