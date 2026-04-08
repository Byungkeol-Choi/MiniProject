package com.cafe.kiosk.dto;

import java.util.List;

/**
 * 관리자 주문 상세 모달용 JSON 응답.
 * 엔티티를 그대로 노출하지 않고 필요한 필드만 담아 직렬화·보안을 단순화한다.
 */
public record OrderAdminDetailDto(
        long id,
        String createdAt,
        String status,
        int totalAmount,
        int discountAmount,
        String paymentMethod,
        MemberInfo member,
        List<LineItem> items
) {
    public record MemberInfo(Long id, String name, String displayPhone) {}

    public record LineItem(String menuName, int quantity, int unitPrice, int lineTotal) {}
}
