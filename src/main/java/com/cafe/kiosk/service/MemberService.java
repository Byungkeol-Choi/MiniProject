package com.cafe.kiosk.service;

import com.cafe.kiosk.domain.Coupon;
import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.dto.MemberCouponDto;
import com.cafe.kiosk.dto.MemberLookupResponse;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 회원 조회 및 키오스크 스탬프(포인트 적립·보상 쿠폰) 처리.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final CouponService couponService;

    /**
     * 전화번호로 회원을 찾는다. 없으면 {@code null}.
     * 입력은 하이픈 등과 무관하게 숫자만 정규화한 뒤 DB와 매칭한다.
     */
    public Member findByPhone(String phone) {
        if (phone == null) {
            return null;
        }
        String normalized = phone.replaceAll("[^0-9]", "");
        if (normalized.isEmpty()) {
            return null;
        }
        // DB에 숫자만 저장된 경우 네이티브 regexp 조회 없이 매칭(풀러 환경에서 부담 감소)
        return memberRepository.findByPhone(normalized)
                .or(() -> memberRepository.findByPhoneNormalized(normalized))
                .orElse(null);
    }

    /**
     * PK로 회원을 조회한다. 존재하지 않으면 {@link Optional#empty()}.
     */
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    /**
     * 전화번호로 가입 회원의 누적 포인트·쿠폰 목록을 조회한다. DB 갱신 없음(조회 전용).
     *
     * @return 미가입이면 {@link Optional#empty()}
     */
    public Optional<MemberLookupResponse> lookupMemberSummary(String phone) {
        Member member = findByPhone(phone);
        if (member == null) {
            return Optional.empty();
        }

        long unusedCouponCount = 0;
        List<MemberCouponDto> coupons = Collections.emptyList();
        try {
            unusedCouponCount = couponRepository.countByMemberIdAndUsedFalse(member.getId());
            List<Coupon> couponList = couponRepository.findByMemberIdOrderByCreatedAtDesc(member.getId());
            coupons = couponList.stream()
                    .map(c -> new MemberCouponDto(
                            c.getName(),
                            c.getCode(),
                            c.isUsed(),
                            c.getDiscountType() != null ? c.getDiscountType().name() : "FIXED",
                            c.getDiscountValue()))
                    .toList();
        } catch (Exception e) {
            log.warn("회원 쿠폰 목록 조회 실패 memberId={}", member.getId(), e);
        }

        // 이름이 정상적으로 있으면 이름을 보여주고, 이름이 비어있거나 공백이면 전화번호를 대체 표시값으로 사용.
        String displayName = (member.getName() != null && !member.getName().isBlank())
                ? member.getName()
                : member.getPhone();

        return Optional.of(new MemberLookupResponse(
                displayName,
                member.getPoints(),
                unusedCouponCount,
                coupons));
    }

    /**
     * 키오스크 스탬프 처리: 포인트를 적립하고, 누적이 3,000P 이상이면 그만큼 차감하며
     * 보상 쿠폰을 반복 발급한다({@link CouponService#issueStampRewardCoupon}).
     * 미가입 전화번호면 예외를 던진다.
     *
     * @param phone        회원 전화번호
     * @param earnedPoints 이번에 적립할 포인트
     * @return 갱신된 회원 엔티티(남은 포인트·발급된 쿠폰 반영 후)
     */
    @Transactional
    public Member processStamp(String phone, int earnedPoints) {
        Member member = findByPhone(phone);
        if (member == null) {
            throw new IllegalArgumentException("가입되지 않은 전화번호입니다.");
        }

        member.setPoints(member.getPoints() + earnedPoints);

        final int COUPON_THRESHOLD = 3000;
        while (member.getPoints() >= COUPON_THRESHOLD) {
            member.setPoints(member.getPoints() - COUPON_THRESHOLD);
            couponService.issueStampRewardCoupon(member);
        }

        return member;
    }
}
