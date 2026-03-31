package com.cafe.kiosk.controller;

import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final CouponRepository couponRepository;

    /** GET / — 루트 접속 시 키오스크로 이동 */
    @GetMapping("/")
    public String root() {
        return "redirect:/kiosk";
    }

    /**
     * GET /member/login
     * 포인트 적립 화면 (kiosk/stamp.html)
     * 필요 변수: earnedPoints
     */
    @GetMapping("/member/login")
    public String stampForm(@RequestParam(required = false, defaultValue = "0") int earnedPoints,
                            Model model) {
        model.addAttribute("earnedPoints", earnedPoints);
        return "kiosk/stamp";
    }

    /**
     * POST /member/stamp
     * 전화번호로 회원 조회 → 포인트 적립 + 쿠폰 자동 발급 (MemberService.processStamp)
     * 파라미터: phone, earnedPoints
     */
    @PostMapping("/member/stamp")
    public String processStamp(@RequestParam String phone,
                               @RequestParam(defaultValue = "0") int earnedPoints,
                               Model model) {
        model.addAttribute("earnedPoints", earnedPoints);

        try {
            Member member = memberService.processStamp(phone, earnedPoints);

            String displayName = (member.getName() != null && !member.getName().isBlank())
                    ? member.getName() : phone;

            model.addAttribute("success", true);
            model.addAttribute("memberName", displayName);
            model.addAttribute("totalPoints", member.getPoints());
            model.addAttribute("coupons", couponRepository.findByMemberIdOrderByCreatedAtDesc(member.getId()));

        } catch (IllegalArgumentException e) {
            model.addAttribute("success", false);
            model.addAttribute("message", e.getMessage());
        } catch (Exception e) {
            log.error("포인트 적립 오류 phone={}", phone, e);
            model.addAttribute("success", false);
            model.addAttribute("message", "포인트 적립 중 오류가 발생했습니다.");
        }
        return "kiosk/stamp";
    }
}
