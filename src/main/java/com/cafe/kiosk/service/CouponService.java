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
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 코드입니다.")); // orElseThrow(): 조회 결과가 있으면 Coupon 객체를 반환하고, 없으면(Optional.empty() 이건 기본값) 즉시 예외를 발생시킵니다.

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
        if (!StringUtils.hasText(rawCode)) { // StringUtils.hasText(rawCode): Spring에서 제공하는 유틸리티 메서드입니다. rawCode가 null이 아니어야 하고, 길이가 0보다 커야 하며, 공백 문자(space)로만 이루어져 있지 않은지를 한 번에 체크합니다.
            return;
        }
        String code = rawCode.trim(); // trim(): 사용자 실수로 입력된 앞뒤 공백을 제거하여 데이터의 정합성을 높입니다.
        Coupon coupon = validateCoupon(code);
        coupon.setUsed(true);
    }

    /**
     * 관리자가 특정 회원에게 수동으로 쿠폰을 발급한다.
     */
    @Transactional
    public Coupon issueCouponByAdmin(Member member, String name, DiscountType discountType,
                                     int discountValue, LocalDateTime expiresAt) {
        return saveNewCoupon(member, name, discountType, discountValue, expiresAt);
    }

    /**
     * 관리자가 쿠폰을 삭제한다.
     */
    @Transactional
    public void deleteCoupon(Long couponId) {
        couponRepository.deleteById(couponId);
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
