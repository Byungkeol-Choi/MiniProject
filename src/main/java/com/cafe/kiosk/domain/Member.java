package com.cafe.kiosk.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(length = 100)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private int points = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    // 최병걸 추가 시작
    /** 화면 표시용 (예: 01011112222 → 010-1111-2222). DB 저장 값은 변경하지 않음. */
    public String getDisplayPhone() {
        if (phone == null || phone.isBlank()) {
            return "";
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }
        if (digits.length() == 10) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        }
        return phone;
    }
    // 최병걸 추가 종료
}