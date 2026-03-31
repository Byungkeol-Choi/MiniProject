package com.cafe.kiosk.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="menu")
@Getter
@NoArgsConstructor
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false, updatable = false)
    private Long id;

    @Column(name="name", nullable = false) //메뉴명
    private String name;
    @Column(name="price", nullable = false) //가격
    private int price;
    @Enumerated(EnumType.STRING) //분류
    @Column(name="category", nullable = false)
    private Category category;
    @Column(name="image_url") //이미지
    private String imageUrl;
    @Column(name="description") //설명
    private String description;
    @Column(name="available", nullable = false) //품절여부
    private Boolean available;
    @Column(name="created_at", nullable = false) //등록일
    private LocalDateTime createdAt;

    public enum Category {FOOD, DRINK};
}
