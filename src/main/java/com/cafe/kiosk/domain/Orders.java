package com.cafe.kiosk.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) //회원테이블 N:1
    @JoinColumn(name="member_id")
    private Member member;

    @Column(name="total_amount", nullable = false) //총결제금액
    private int totalAmount;
    @Column(name="discount_amount", nullable = false) //할인금액
    private int discountAmount;
    @Column(name="payment_method") //결재방법
    private String paymentMethod;
    @Enumerated(EnumType.STRING) //주문상태 = default:RECEIVED
    @Column(name="status", nullable = false)
    private Status status;
    @Column(name="created_at", nullable = false) //주문시간
    private LocalDateTime createdAt;

    @OneToMany(mappedBy="orders", cascade=CascadeType.ALL, orphanRemoval = true) //주문상세테이블 1:N
    @Builder.Default
    private List<OrderItem> orderItem = new ArrayList<>();

    public enum Status {RECEIVED, PREPARING, COMPLETED, CANCELLED};
}
