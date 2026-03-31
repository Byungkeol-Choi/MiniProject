package com.cafe.kiosk.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="coupon")
@Getter
@NoArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    @Column(name="code", nullable = false, unique = true) //쿠폰코드
    private String code;
    @Column(name="discount_type", nullable=false) //할인유형
    private String discountType;
    @Column(name="discount_value", nullable = false) //할인금액
    private int discountValue;
    @Column(name="used", nullable = false) //사용여부
    private Boolean used;
    @Column(name="expires_at") //발급일
    private LocalDateTime expiresAt;
    @Column(name="created_at", nullable = false) //사용일
    private LocalDateTime createdAt;
}
