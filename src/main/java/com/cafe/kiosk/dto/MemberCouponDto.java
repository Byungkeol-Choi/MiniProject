package com.cafe.kiosk.dto;

/**
 * 조회 응답에 포함되는 쿠폰 한 건(표시용).
 */
public record MemberCouponDto(
        String name,
        String code,
        boolean used,
        String discountType,
        int discountValue
) {
}
