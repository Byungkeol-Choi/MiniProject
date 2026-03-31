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
@Table(name="member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false, updatable = false)
    private Long id;

    @Column(name="phone", nullable = false, unique = true) //전화번호
    private String phone;
    @Column(name="name") //이름
    private String name;
    @Column(name="points", nullable = false) //보유포인트
    private int points;
    @Column(name="created_at", nullable = false) //가입일
    private LocalDateTime createdAt;

    @OneToMany(mappedBy="member", cascade=CascadeType.ALL, orphanRemoval=true) //주문테이블 1:N
    @Builder.Default
    private List<Orders> orders = new ArrayList<>();

    @OneToMany(mappedBy="member", cascade=CascadeType.ALL, orphanRemoval=true) //쿠폰테이블 1:N
    @Builder.Default
    private List<Coupon> coupons = new ArrayList<>();
}
