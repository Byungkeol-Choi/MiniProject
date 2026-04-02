package com.cafe.kiosk.dto;

import java.util.List;

/**
 * 전화번호로 조회한 회원의 누적 포인트·미사용 쿠폰 수·쿠폰 목록.
 */
public record MemberLookupResponse(
        Long memberId,
        String memberName,
        int points,
        long unusedCouponCount,
        List<MemberCouponDto> coupons
) {
}

//@Getter
//@NoArgsConstructor // 기본 생성자 자동 생성
//@AllArgsConstructor // 모든 필드를 받는 생성자 자동 생성
//public class MemberLookupResponse {
//    private Long memberId;
//    private String memberName;
//    private int points;
//    private long unusedCouponCount;
//    private List<MemberCouponDto> coupons;
//
//}