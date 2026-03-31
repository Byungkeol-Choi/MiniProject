package com.cafe.kiosk.repository;

import com.cafe.kiosk.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /** 미사용 쿠폰 코드로 조회 */
    Optional<Coupon> findByCodeAndUsedFalse(String code);

    /** 코드 중복 확인 */
    boolean existsByCode(String code);

    /** 회원의 전체 쿠폰 목록 (최신순) */
    List<Coupon> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    /** 회원의 미사용 쿠폰 수 */
    long countByMemberIdAndUsedFalse(Long memberId);

    /** 전체 미사용 쿠폰 수 */
    long countByUsedFalse();
}
