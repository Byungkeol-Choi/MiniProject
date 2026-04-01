package com.cafe.kiosk.dto;

import java.util.List;

/**
 * 스탬프 적립 REST API 응답 본문.
 */
public record StampResponse(
        boolean success,
        String memberName,
        int totalPoints,
        int earnedPoints,
        List<MemberCouponDto> coupons,
        String message
) {
}
