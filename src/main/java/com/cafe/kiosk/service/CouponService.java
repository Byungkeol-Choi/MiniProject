package com.cafe.kiosk.service;

import com.cafe.kiosk.domain.Coupon;
import com.cafe.kiosk.domain.Coupon.DiscountType;
import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 쿠폰 코드 검증, 사용 처리, 스탬프 보상 발급 등 쿠폰 관련 로직.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;

    /**
     * 결제·장바구니 등에서 입력한 코드가 쓸 수 있는지 확인한다.
     * DB에서 미사용({@code used = false}) 쿠폰만 조회하고, 만료일이 지났으면 예외를 던진다.
     *
     * @return 검증된 쿠폰 엔티티
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
     * 결제 확정 시 코드로 미사용·유효 쿠폰을 소진한다({@code used = true}).
     * 코드가 비어 있으면 아무 것도 하지 않는다.
     */
    @Transactional
    public void redeemCouponByCode(String rawCode) {
        if (!StringUtils.hasText(rawCode)) {
            return;
        }
        String code = rawCode.trim();
        Coupon coupon = validateCoupon(code);
        coupon.setUsed(true);
    }

    /**
     * 스탬프 룰에 따라 회원에게 보상 쿠폰 1건을 저장한다.
     * {@link MemberService#processStamp} 안에서 포인트 3,000P 차감과 짝을 이룰 때 호출된다.
     */
    @Transactional
    public Coupon issueStampRewardCoupon(Member member) {
        return saveNewCoupon(member, "3,000원 할인 쿠폰", DiscountType.FIXED, 3000,
                LocalDateTime.now().plusMonths(1));
    }

    /**
     * 공통 쿠폰 엔티티 생성·저장. 코드는 {@link #generateUniqueCode()}로 중복 없이 부여한다.
     */
    private Coupon saveNewCoupon(Member member, String name, DiscountType discountType,
                                 int discountValue, LocalDateTime expiresAt) {
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

    /**
     * {@code CAFE-XXXXXXXX} 형식 문자열을 만들고, DB에 이미 있으면 다시 뽑는다.
     */
    private String generateUniqueCode() {
        String code;
        do {
            code = "CAFE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (couponRepository.existsByCode(code));
        return code;
    }
}
