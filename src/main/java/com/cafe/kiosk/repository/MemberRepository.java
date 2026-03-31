package com.cafe.kiosk.repository;

import com.cafe.kiosk.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByPhone(String phone);

    /** DB에 저장된 형식(하이픈 유무)에 관계없이 숫자만 비교해서 조회 */
    @Query(value = "SELECT * FROM member WHERE REPLACE(phone, '-', '') = :phone", nativeQuery = true)
    Optional<Member> findByPhoneNormalized(@Param("phone") String phone);
}
