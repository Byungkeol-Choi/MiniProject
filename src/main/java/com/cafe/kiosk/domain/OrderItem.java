package com.cafe.kiosk.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="order_item")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) //주문테이블 N:1
    @JoinColumn(name="order_id")
    private Orders orders;

    @ManyToOne(fetch=FetchType.LAZY) //메뉴테이블 N:1
    @JoinColumn(name="menu_id")
    private Menu menu;

    @Column(name="quantity") //주문수량
    private int quantity;
    @Column(name="unit_price") //단가
    private int unitPrice;


    public OrderItem(Orders orders, Menu menu, int quantity, int unitPrice){
        this.orders = orders;
        this.menu = menu;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}
