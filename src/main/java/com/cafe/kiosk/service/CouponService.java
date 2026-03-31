package com.cafe.kiosk.service;

import com.cafe.kiosk.domain.Coupon;
import com.cafe.kiosk.domain.Coupon.DiscountType;
import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;

    /**
     * 쿠폰 코드 검증.
     * 미사용 + 만료 기간 이내인 경우에만 유효.
     */
    public Coupon validateCoupon(String code) {
        Coupon coupon = couponRepository.findByCodeAndUsedFalse(code)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 코드입니다."));

        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }
        return coupon;
    }

    /**
     * 할인 금액 계산.
     * FIXED: 정액 할인 (originalAmount 초과 불가)
     * PERCENT: 퍼센트 할인
     */
    public int calculateDiscount(Coupon coupon, int originalAmount) {
        if (coupon.getDiscountType() == DiscountType.FIXED) {
            return Math.min(coupon.getDiscountValue(), originalAmount);
        }
        return (int) Math.round(originalAmount * coupon.getDiscountValue() / 100.0);
    }

    /** 쿠폰 사용 처리 (used = true) */
    @Transactional
    public void useCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        coupon.setUsed(true);
    }

    /**
     * 관리자 쿠폰 발급.
     * 코드는 UUID 기반으로 자동 생성.
     */
    @Transactional
    public Coupon issueCoupon(Long memberId, String name, DiscountType discountType,
                              int discountValue, LocalDateTime expiresAt) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id=" + memberId));

        Coupon coupon = Coupon.builder()
                .member(member)
                .name(name)
                .code(generateUniqueCode())
                .discountType(discountType)
                .discountValue(discountValue)
                .expiresAt(expiresAt)
                .build();

        return couponRepository.save(coupon);
    }

    /** 코드 중복 없는 유니크 코드 생성 */
    private String generateUniqueCode() {
        String code;
        do {
            code = "CAFE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (couponRepository.existsByCode(code));
        return code;
    }
}
