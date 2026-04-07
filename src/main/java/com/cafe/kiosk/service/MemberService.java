package com.cafe.kiosk.service;

import com.cafe.kiosk.domain.Coupon;
import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.dto.MemberCouponDto;
import com.cafe.kiosk.dto.MemberLookupResponse;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;


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
    private final EntityManager entityManager;

    /**
     * 전화번호로 회원을 찾는다. 없으면 {@code null}.
     * 입력은 하이픈 등과 무관하게 숫자만 정규화한 뒤 DB와 매칭한다.
     */
    public Member findByPhone(String phone) {
        if (phone == null) {
            return null;
        }
        String normalized = phone.replaceAll("[^0-9]", ""); // 정규표현식 [^0-9]를 사용하여 숫자가 아닌 모든 문자(하이픈 -, 공백, 괄호 등)를 제거합니다.
        if (normalized.isEmpty()) {
            return null;
        }
        // DB에 숫자만 저장된 경우 네이티브 regexp 조회 없이 매칭(풀러 환경에서 부담 감소)
        return memberRepository.findByPhone(normalized) // 1차
                .or(() -> memberRepository.findByPhoneNormalized(normalized)) // 2차
                .orElse(null);
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
            unusedCouponCount = couponRepository.countByMemberIdAndUsedFalse(member.getId()); // 회원의 "미사용이면서 만료되지 않은" 쿠폰 개수를 DB에서 바로 집계해서 가져온다.
            List<Coupon> couponList = couponRepository.findByMemberIdOrderByCreatedAtDesc(member.getId()); // 회원의 전체 쿠폰 목록을 (최신 순으로) 가져온다. (사용/만료 여부 상관없이 전부)
            coupons = couponList.stream()
                    .filter(Coupon::isCurrentlyUsable) // 전체 쿠폰 목록 중에서 "지금 사용 가능한 쿠폰"만 필터링한다.
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

        // MemberLookupResponse 객체로 생성한 뒤, 이를 Optional 상자에 담아 최종적으로 반환합니다.
        return Optional.of(new MemberLookupResponse(
                member.getId(),
                displayName,
                member.getPoints(),
                unusedCouponCount,
                coupons));
    }

    /**
     * 키오스크 스탬프 처리: 포인트를 적립한다. 포인트는 차감 없이 누적된다.
     * 쿠폰 발급은 관리자 페이지에서 수동으로 진행한다.
     * 미가입 전화번호면 예외를 던진다.
     *
     * @param phone        회원 전화번호
     * @param earnedPoints 이번에 적립할 포인트
     * @return 갱신된 회원 엔티티
     */
    @Transactional
    public Member processStamp(String phone, int earnedPoints) {

        // DB에서 읽어온 이 member 객체는 일반적인 자바 객체가 아닙니다.
        // JPA는 DB에서 가져온 데이터를 자바 객체로 만든 뒤, **"이 객체는 지금부터 내가 관리한다"며 영속성 컨텍스트에 등록(스냅샷 저장)**합니다. 이제 member는 영속 객체가 되었습니다.
        Member member = findByPhone(phone);
        if (member == null) {
            throw new IllegalArgumentException("가입되지 않은 전화번호입니다.");
        }

        member.setPoints(member.getPoints() + earnedPoints);

        return member;
    }

    /**
     * 키오스크 결제 시 포인트를 차감한다.
     *
     * @param memberId       회원 ID
     * @param pointsToDeduct 차감할 포인트
     */
    @Transactional
    public void deductPoints(Long memberId, int pointsToDeduct) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        if (member.getPoints() < pointsToDeduct) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        member.setPoints(member.getPoints() - pointsToDeduct);
    }

    /**
     * 관리자: 회원 추가
     * - 전화번호는 숫자만 정규화해서 저장(하이픈/공백 무시)
     * - 같은 전화번호가 이미 있으면 409로 처리되도록 {@link IllegalStateException} 발생
     */
    @Transactional
    public Member createMemberByAdmin(String phoneRaw, String name) {
        String normalizedPhone = normalizePhone(phoneRaw);
        if (normalizedPhone.length() < 10 || normalizedPhone.length() > 11) {
            throw new IllegalArgumentException("전화번호는 10~11자리 숫자로 입력해주세요.");
        }

        Member existing = findByPhone(normalizedPhone);
        if (existing != null) {
            throw new IllegalStateException("이미 가입된 전화번호입니다.");
        }

        // **Dirty checking(변경 감지)**은 이미 영속(managed) 상태인 엔티티에만 해당합니다.
        // 즉, 이번 트랜잭션 안에서 DB에서 읽어 온 뒤 같은 EntityManager가 관리하는 객체를 수정했을 때, flush 시점에 UPDATE가 나갑니다.

        Member member = Member.builder()
                .phone(normalizedPhone)
                .name((name != null && !name.isBlank()) ? name.trim() : null)
                .build();

        // Member.builder()…build()로 방금 만든 새 객체이고
        // 아직 영속 컨텍스트에 등록되지 않은(transient) 상태입니다.
        // 이 상태에서는 필드를 바꿔도 Hibernate가 “새 행을 INSERT해야 한다”고 자동으로 알 수 없습니다. INSERT는 persist / save로 엔티티를 영속화할 때 처리됩니다.
        // 즉, 자바 코드상에서는 똑같은 Member 타입이라도, JPA가 개입하느냐 아니냐에 따라 **"상태"**가 달라집니다. (Managed상태가 되야 JPA한테 관리받음)

        return memberRepository.save(member); // memberRepository.save()는 언제나 **"EntityManager가 현재 확실하게 관리하고 있는 최신 상태의 영속(Managed) 객체"**를 반환하도록 설계되어 있습니다.
    }

    /**
     * 관리자: 회원 삭제(쿠폰/주문이 있으면 거부)
     * - 쿠폰이 하나라도 있으면 삭제 불가
     * - 주문이 하나라도 있으면 삭제 불가
     */
    @Transactional
    public void deleteMemberByAdminWithPolicy(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID가 필요합니다.");
        }

        Member member = memberRepository.findById(memberId).orElseThrow(NoSuchElementException::new); // .orElseThrow(() -> new NoSuchElementException()); 과 동일한 람다식.

        boolean hasCoupons = !couponRepository
                .findByMemberIdOrderByCreatedAtDesc(memberId)
                .isEmpty();

        // OrdersRepository를 수정하지 않고, JPQL로 주문 존재 여부를 확인한다.
        Long orderCount = entityManager.createQuery(
                        "select count(o) from Orders o where o.member.id = :memberId", Long.class)
                .setParameter("memberId", member.getId())
                .getSingleResult();

        boolean hasOrders = orderCount != null && orderCount > 0;

        if (hasCoupons || hasOrders) {
            if (hasCoupons && hasOrders) {
                throw new IllegalStateException("해당 회원은 쿠폰과 주문이 존재하여 삭제할 수 없습니다.");
            }
            if (hasCoupons) {
                throw new IllegalStateException("해당 회원은 쿠폰이 존재하여 삭제할 수 없습니다.");
            }
            throw new IllegalStateException("해당 회원은 주문이 존재하여 삭제할 수 없습니다.");
        }

        memberRepository.delete(member);
    }

    private String normalizePhone(String phoneRaw) {
        if (phoneRaw == null) {
            return "";
        }
        String normalized = phoneRaw.replaceAll("[^0-9]", "");
        return normalized;
    }
}
