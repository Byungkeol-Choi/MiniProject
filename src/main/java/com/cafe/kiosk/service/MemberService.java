package com.cafe.kiosk.service;

import com.cafe.kiosk.domain.Coupon;
import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;

    /**
     * 전화번호로 회원 조회 — 없으면 null
     * DB 저장 형식(하이픈 유무)에 관계없이 숫자만 비교해서 조회
     */
    public Member findByPhone(String phone) {
        String normalized = phone.replaceAll("[^0-9]", "");
        return memberRepository.findByPhoneNormalized(normalized).orElse(null);
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    /** 포인트 적립 */
    @Transactional
    public Member addPoints(Long memberId, int points) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id=" + memberId));
        member.setPoints(member.getPoints() + points);
        return member;
    }

    /** 포인트 차감 */
    @Transactional
    public Member deductPoints(Long memberId, int points) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id=" + memberId));
        if (member.getPoints() < points) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        member.setPoints(member.getPoints() - points);
        return member;
    }

    /**
     * 포인트 적립 + 쿠폰 자동 발급
     * 누적 포인트가 3,000P 이상이 되면 3,000원 할인 쿠폰 자동 발급 (초과 시 반복)
     *
     * @param phone        회원 전화번호
     * @param earnedPoints 이번 주문으로 적립할 포인트
     * @return 업데이트된 회원 엔티티
     */
    @Transactional
    public Member processStamp(String phone, int earnedPoints) {
        Member member = findByPhone(phone);
        if (member == null) {
            throw new IllegalArgumentException("가입되지 않은 전화번호입니다.");
        }

        member.setPoints(member.getPoints() + earnedPoints);

        final int COUPON_THRESHOLD = 3000;
        while (member.getPoints() >= COUPON_THRESHOLD) {
            member.setPoints(member.getPoints() - COUPON_THRESHOLD);

            String couponCode = "CAFE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            couponRepository.save(Coupon.builder()
                    .member(member)
                    .name("3,000원 할인 쿠폰")
                    .code(couponCode)
                    .discountType(Coupon.DiscountType.FIXED)
                    .discountValue(3000)
                    .used(false)
                    .expiresAt(LocalDateTime.now().plusMonths(1))
                    .build());
        }

        return member;
    }
}
