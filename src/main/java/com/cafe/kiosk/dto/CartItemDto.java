package com.cafe.kiosk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Long id; //menu-id
    private String name; //menu-name
    private int quantity; //orderItem-quantity
    private int price; //orderItem-unitPrice
    private String imgUrl; //menu-imageUrl
}
