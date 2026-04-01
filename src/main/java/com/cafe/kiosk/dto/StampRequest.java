package com.cafe.kiosk.dto;

/**
 * 스탬프 적립 REST API 요청 본문.
 */
public record StampRequest(String phone, int earnedPoints) {
}
