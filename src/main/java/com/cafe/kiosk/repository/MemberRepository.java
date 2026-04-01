package com.cafe.kiosk.repository;

import com.cafe.kiosk.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByPhone(String phone);

    /**
     * Java {@code replaceAll("[^0-9]", "")} 와 동일하게, DB 쪽 전화번호에서 숫자만 남겨 비교 (PostgreSQL).
     * 하이픈·공백 등으로 인한 미매칭·조회 실패를 줄인다.
     */
    @Query(value = "SELECT * FROM member WHERE regexp_replace(coalesce(phone, ''), '[^0-9]', '', 'g') = :phone LIMIT 1", nativeQuery = true)
    Optional<Member> findByPhoneNormalized(@Param("phone") String phone);
}
