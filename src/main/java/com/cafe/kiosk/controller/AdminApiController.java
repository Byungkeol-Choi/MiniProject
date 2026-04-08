package com.cafe.kiosk.controller;

import com.cafe.kiosk.domain.Coupon;
import com.cafe.kiosk.domain.Coupon.DiscountType;
import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.service.MemberService;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.repository.MemberRepository;
import com.cafe.kiosk.service.CouponService;
import com.cafe.kiosk.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 관리자 페이지용 쿠폰 CRUD REST API.
 * /admin/** 경로이므로 SecurityConfig에 의해 ROLE_ADMIN 인증이 강제된다.
 */
@RestController
@RequestMapping("/admin/api")
@RequiredArgsConstructor
public class AdminApiController {

    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final CouponService couponService;
    private final MemberService memberService;
    private final OrderService orderService;

    /** 관리자 주문 상세 모달용 (JSON). GET이라 CSRF 불필요, 세션 쿠키로 ADMIN 인증. */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(orderService.getAdminOrderDetail(orderId));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** 특정 회원의 쿠폰 목록 조회 */ // members.html 432 lines
    @GetMapping("/members/{memberId}/coupons") // (사용자가 특정 회원 행의「쿠폰 관리」버튼을 클릭). 참고로 쿠폰 관리 클릭은 AJAX 조회이지 주소창이 바뀌는 내비게이션이 아닙니다.
    public ResponseEntity<?> getMemberCoupons(@PathVariable Long memberId) {
        List<Coupon> coupons = couponRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        // 쿠폰 리스트를 스트림으로 변환해 각각을 키-값(Map) 형태로 가공하여 반환. (응답 본문 타입이 길어지는 것을 방지하기 위해 ResponseEntity<?> 와일드카드 사용)
        List<Map<String, Object>> result = coupons.stream().map(c -> Map.<String, Object>of(
                "id", c.getId(),
                "name", c.getName(),
                "code", c.getCode(),
                "discountType", c.getDiscountType().name(),
                "discountValue", c.getDiscountValue(),
                "used", c.isUsed(),
                "expiresAt", c.getExpiresAt() != null ? c.getExpiresAt().toString() : "",
                "createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : ""
        )).toList();
        return ResponseEntity.ok(result);
    }

    // record: 불변(Immutable) 데이터를 다루기 위해 자바 14부터 도입된 문법입니다.
    // 괄호 안에 필드만 나열하면 컴파일러가 알아서 생성자(전체 생성자), toString, equals, 그리고 데이터 조회용 메서드를 자동으로 만들어 줍니다.
    // 주의할 점은 기존의 getName() 같은 방식이 아니라, req.name(), req.expiresAt()처럼 필드 이름 자체가 메서드 이름이 된다는 점입니다.
    record CouponCreateRequest(String name, String discountType, int discountValue, String expiresAt) {}

    /** 특정 회원에게 쿠폰 발급 */
    // 쿠폰 발급 버튼을 클릭시 프론트JS가 fetch(POST /members/{memberId}/coupons) 요청을 보낸다.”
    @PostMapping("/members/{memberId}/coupons")
    public ResponseEntity<?> createCoupon(@PathVariable Long memberId,
                                            @RequestBody CouponCreateRequest req) {
        Member member = memberRepository.findById(memberId).orElseThrow();

        LocalDateTime expiresAt = (req.expiresAt() != null && !req.expiresAt().isBlank())
                ? LocalDateTime.parse(req.expiresAt())
                : LocalDateTime.now().plusMonths(1);

        Coupon coupon = couponService.issueCouponByAdmin(member, req.name(),
                DiscountType.valueOf(req.discountType().toUpperCase()),
                req.discountValue(), expiresAt);

        return ResponseEntity.ok(Map.of(
                "id", coupon.getId(),
                "code", coupon.getCode(),
                "name", coupon.getName()
        ));
    }

    /** 쿠폰 삭제 */
    @DeleteMapping("/coupons/{couponId}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        // 스프링 내부의 메시지 컨버터(Jackson)가 자바의 Map 객체를 자동으로 JSON 형식으로 변환해 줍니다.
        return ResponseEntity.ok(Map.of("message", "쿠폰이 삭제되었습니다.")); // Map.of로 만든 맵은 생성 후 데이터를 추가하거나 수정할 수 없습니다.
    }


    record MemberCreateRequest(String phone, String name) {}

    /** 관리자: 회원 추가 */
    @PostMapping("/members")
    public ResponseEntity<?> createMember(@RequestBody MemberCreateRequest req) {
        try {
            Member created = memberService.createMemberByAdmin(req.phone(), req.name());
            return ResponseEntity.status(201).body(Map.of(
                    "id", created.getId(),
                    "phone", created.getPhone(),
                    "name", created.getName(),
                    "points", created.getPoints()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    /** 관리자: 회원 삭제(쿠폰/주문 존재 시 거부) */
    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<?> deleteMember(@PathVariable Long memberId) {
        try {
            memberService.deleteMemberByAdminWithPolicy(memberId);
            return ResponseEntity.ok(Map.of("message", "회원이 삭제되었습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
