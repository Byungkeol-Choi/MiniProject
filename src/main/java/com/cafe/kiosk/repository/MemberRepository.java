package com.cafe.kiosk.repository;

import com.cafe.kiosk.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByPhone(String phone);

    /**
     * Java {@code replaceAll("[^0-9]", "")} 와 동일하게, DB 쪽 전화번호에서 숫자만 남겨 비교 (PostgreSQL).
     * 하이픈·공백 등으로 인한 미매칭·조회 실패를 줄인다.
     */
    // Java가 아니라 데이터베이스 엔진(PostgreSQL) 내부에서 실시간으로 정규화를 수행합니다.
    @Query(value = "SELECT * FROM member WHERE regexp_replace(coalesce(phone, ''), '[^0-9]', '', 'g') = :phone LIMIT 1", nativeQuery = true)
    Optional<Member> findByPhoneNormalized(@Param("phone") String phone);

    // 최병걸 추가 시작
    @Query(value = """
        SELECT COUNT(*)
        FROM member
        WHERE created_at >= :start
          AND created_at < :end
        """, nativeQuery = true)
    long countByDateBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    default long countByMemberForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return countByDateBetween(start, end);
    }
    // 최병걸 추가 종료
}
