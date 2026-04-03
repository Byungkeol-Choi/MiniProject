package com.cafe.kiosk.repository;

import com.cafe.kiosk.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /** 미사용 쿠폰 코드로 조회 */
    Optional<Coupon> findByCodeAndUsedFalse(String code);

    /** 코드 중복 확인 */
    boolean existsByCode(String code);

    /**
     * 회원의 전체 쿠폰 목록 (최신순).
     * 엔티티는 {@code member} 참조만 있으므로 파생 쿼리 {@code findByMemberId...}는 런타임 오류를 낼 수 있어 JPQL로 고정한다.
     */
    @Query("SELECT c FROM Coupon c WHERE c.member.id = :memberId ORDER BY c.createdAt DESC")
    List<Coupon> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);

    /** 회원별 미사용·미만료 쿠폰 수 ({@code expiresAt}이 null이면 무기한 유효). */
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.member.id = :memberId AND c.used = false "
            + "AND (c.expiresAt IS NULL OR c.expiresAt >= CURRENT_TIMESTAMP)") // 쿠폰 만료시간 지난것도 검증 추가.
    long countByMemberIdAndUsedFalse(@Param("memberId") Long memberId);

    /** 전체 미사용·미만료 쿠폰 수 */
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.used = false "
            + "AND (c.expiresAt IS NULL OR c.expiresAt >= CURRENT_TIMESTAMP)") // 쿠폰 만료시간 지난것도 검증 추가.
    long countByUsedFalse();
}
