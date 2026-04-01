package com.cafe.kiosk.controller;

import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.service.MemberService;
import jakarta.servlet.http.HttpSession;
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

    /**
     * GET /member/stamp
     * complete.html 포인트 적립 버튼에서 진입
     * 세션의 finalAmount를 읽어 5% 포인트 계산 후 전달
     */
    @GetMapping("/member/stamp")
    public String stampForm(HttpSession session, Model model) {
        Integer finalAmount = (Integer) session.getAttribute("finalAmount");
        int earnedPoints = (finalAmount != null) ? (int) (finalAmount * 0.05) : 0;
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

            model.addAttribute("success", true); // success라는 이름으로 값 true가 Model에 들어갑니다.
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

// kiosk/stamp.html 파일 하나 안에 두 가지 덩어리가 있고, success가 true인지 아닌지로 어느 쪽을 보여줄지 나뉩니다.
// th:if="${success}" → 적립 완료 카드(이름, 적립·누적 포인트, 쿠폰 목록, 처음으로)
// th:unless="${success}" → 전화번호 입력·키패드·적립 포인트 입력·POST /member/stamp 폼
// GET /member/login으로 처음 들어올 때는 보통 success를 안 넣거나 false라서 입력 화면이 나오고,
// POST /member/stamp 처리 후에는 컨트롤러가 success를 true/false로 넣어 같은 템플릿으로 완료 또는 오류(다시 입력 폼)가 됩니다.