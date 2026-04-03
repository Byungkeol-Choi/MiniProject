package com.cafe.kiosk.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @JoinColumn: 이 필드가 다른 테이블과의 **연관 관계(Join)**를 맺기 위한 **외래 키(FK)**임을 명시합니다.
    // 1.참조 대상 확인: Member 객체의 식별자(보통 @Id가 붙은 PK)가 무엇인지 확인합니다.
    // 2.외래 키 설정: coupon 테이블의 member_id 컬럼이 member 테이블의 PK를 가리키도록 설정합니다.
    // 3.제약 조건: 필요한 경우 데이터베이스에 외래 키 제약 조건(FK Constraint)을 생성합니다.
    // 외래 키의 참조 조건: 유일성(Uniqueness)
    // 데이터베이스 설계 규칙상 외래 키(FK)는 참조하는 테이블의 행을 유일하게 식별할 수 있는 컬럼을 가리켜야 합니다.

    // @ManyToOne의 의미
    // 이 어노테이션은 객체 간의 관계를 정의합니다.
    // Many: 현재 엔티티 (예: Coupon)
    // One: 참조하는 엔티티 (예: Member)
    // 의미: 여러 개의 쿠폰이 하나의 회원에게 속할 수 있다는 다대일(N:1) 관계를 나타냅니다.

    // fetch = FetchType.LAZY (지연 로딩)
    // fetch 전략은 **"연관된 객체를 언제 데이터베이스에서 가져올 것인가?"**를 결정합니다.
    // LAZY (지연 로딩): 연관된 엔티티를 실제로 사용할 때 조회합니다.
    // couponRepository.findById(id)를 호출하면 Coupon 데이터만 가져오고, Member는 가져오지 않습니다.
    // EAGER (즉시 로딩): 엔티티를 조회할 때 연관된 엔티티도 한꺼번에 가져옵니다.
    // Coupon을 조회할 때 조인(Join)을 통해 Member 정보까지 즉시 긁어옵니다.

    // 지연 로딩의 마법: 프록시(Proxy) // 프록시: 겉모양은 Member와 똑같지만 내부 데이터는 비어있는 상태입니다.
    // LAZY 설정을 하면 JPA는 member 변수에 실제 Member 객체 대신 **가짜 객체(Proxy)**를 넣어둡니다.
    // Coupon coupon = couponRepository.findById(1L).get();
    // Member member = coupon.getMember(); // 이때까지 member는 가짜(Proxy)입니다.
    // System.out.println(member.getName()); // 실제 데이터를 사용하는 이 순간!
    //                                       // JPA가 DB에 SQL을 날려 Member를 조회합니다.
    // 왜 실무에서는 LAZY를 권장할까? ① 성능 최적화와 리소스 절약, ② N+1 문제 예방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 10)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private int discountValue;

    @Column(nullable = false)
    @Builder.Default
    // @Builder.Default를 써줌으로써, 개발자가 실수로 빌더에서 .used(true/false)를 호출하지 않더라도 항상 안전하게 false라는 값이 할당된 상태로 DB에 저장될 수 있게 보장합니다.
    private boolean used = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum DiscountType {
        FIXED, PERCENT
    }

    /**
     * 미사용이고 만료일이 없거나 아직 지나지 않았으면 {@code true}.
     * 결제 시 {@link com.cafe.kiosk.service.CouponService#validateCoupon(String)}와 동일한 기준입니다.
     */
    // 이 메서드는 CouponRepository 등으로 Coupon 엔티티를 조회한 뒤, 가져온 그 객체의 값으로 사용 가능 여부를 판정한다.
    public boolean isCurrentlyUsable() {
        if (used) { // 이미 사용된 쿠폰이면 true로 false반환.
            return false;
        }
        if (expiresAt == null) { // 미사용 쿠폰인데 만료일이 없으면(null값) true반환.
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        return expiresAt.isEqual(now) || expiresAt.isAfter(now); // “만료 시간이 현재 시각과 같을 때는 아직 사용할 수 있다고 본다”.
    }

}