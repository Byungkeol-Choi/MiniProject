package com.cafe.kiosk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemSessionDto {
    private List<CartItemDto> items;
    private Long memberId;
    private String memberName;
    private String couponCode;
    private Integer couponDiscount;
    private Integer usePoints;
}
