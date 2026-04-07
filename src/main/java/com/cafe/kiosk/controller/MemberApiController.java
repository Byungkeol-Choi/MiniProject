package com.cafe.kiosk.controller;

import com.cafe.kiosk.domain.Coupon;
import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.dto.MemberCouponDto;
import com.cafe.kiosk.dto.MemberLookupRequest;
import com.cafe.kiosk.dto.MemberLookupResponse;
import com.cafe.kiosk.dto.StampRequest;
import com.cafe.kiosk.dto.StampResponse;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 키오스크 회원 조회 및 스탬프 적립 JSON API.
 */
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;
    private final CouponRepository couponRepository;

    /**
     * 전화번호로 가입 회원의 포인트·쿠폰을 조회한다. 적립/차감은 하지 않는다.
     */
    // 호출위치: src/main/resources/static/js/kiosk.js
    @PostMapping("/lookup")
    public ResponseEntity<MemberLookupResponse> lookup(@RequestBody MemberLookupRequest request, HttpSession session) {
        Optional<MemberLookupResponse> found = memberService.lookupMemberSummary(request.phone());
        found.ifPresent(r -> session.setAttribute("memberPhone", request.phone()));
        return found.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 스탬프 페이지 진입 시 세션에서 적립 예정 포인트를 반환한다.
     */
    // complete.html의 포인트 적립 버튼 클릭시 응답 페이지 로드 시점에 실행됩니다.
    // 페이지 열림 → 번호입력 UI 표시 (거의 순간적) 여기까지는 조건 상관없이 동일 → API 응답 후 if memberPhone이 null이 아니면 numpad 숨김/자동입력 표시
    @GetMapping("/stamp-info")
    public ResponseEntity<?> stampInfo(HttpSession session) {
        Integer finalAmount = (Integer) session.getAttribute("finalAmount"); // OrderController 에서 세션에 저장된 마지막 결제금액 가져오면됨.
        int earnedPoints = (finalAmount != null) ? (int) (finalAmount * 0.05) : 0;
        String memberPhone = (String) session.getAttribute("memberPhone");

        Map<String, Object> result = new HashMap<>();
        result.put("earnedPoints", earnedPoints);
        if (memberPhone != null) {
            result.put("memberPhone", memberPhone);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 전화번호로 회원을 조회하고 포인트를 적립한다.
     */
    // 호출위치: kiosk/stamp.html (적립하기 버튼)
    @PostMapping("/stamp")
    public ResponseEntity<StampResponse> stamp(@RequestBody StampRequest request) {
        Member member = memberService.processStamp(request.phone(), request.earnedPoints());

        String displayName = (member.getName() != null && !member.getName().isBlank())
                ? member.getName() : member.getPhone();

        List<MemberCouponDto> coupons = couponRepository
                .findByMemberIdOrderByCreatedAtDesc(member.getId())
                .stream()
                .filter(Coupon::isCurrentlyUsable)
                .map(c -> new MemberCouponDto(
                        c.getName(),
                        c.getCode(),
                        c.isUsed(),
                        c.getDiscountType() != null ? c.getDiscountType().name() : "FIXED",
                        c.getDiscountValue()))
                .toList();

        return ResponseEntity.ok(new StampResponse(
                true, displayName, member.getPoints(), request.earnedPoints(), coupons, null));
    }
}
