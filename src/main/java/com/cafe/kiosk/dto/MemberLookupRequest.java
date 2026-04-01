package com.cafe.kiosk.dto;

/**
 * 장바구니 등에서 회원 포인트·쿠폰 조회 API 요청 본문.
 */
public record MemberLookupRequest(String phone) {
}
