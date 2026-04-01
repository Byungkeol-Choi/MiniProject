package com.cafe.kiosk.dto;

import java.util.List;

/**
 * 전화번호로 조회한 회원의 누적 포인트·미사용 쿠폰 수·쿠폰 목록.
 */
public record MemberLookupResponse(
        String memberName,
        int points,
        long unusedCouponCount,
        List<MemberCouponDto> coupons
) {
}
