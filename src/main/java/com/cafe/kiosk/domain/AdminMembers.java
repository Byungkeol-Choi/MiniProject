package com.cafe.kiosk.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="admin")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMembers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String username;
    private String password;
    private LocalDateTime createdAt;

}

//id         BIGSERIAL PRIMARY KEY,
//username   VARCHAR(50)  NOT NULL UNIQUE,
//password   VARCHAR(255) NOT NULL,
//created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP