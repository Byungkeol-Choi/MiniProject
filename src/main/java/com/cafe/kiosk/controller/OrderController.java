package com.cafe.kiosk.controller;

import com.cafe.kiosk.domain.Coupon;
import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final MemberService memberService;
    private final CouponRepository couponRepository;

    /** discountType: PERCENT | FIXED (쿠폰 할인 계산용) */
    public record CouponLookupRow(String code, String title, String benefit, String discountType, int discountValue) {}

    /**
     * 구 URL 호환: /order/cart?phone= 으로 통합
     */
    @GetMapping("/cart/lookup-result")
    public String legacyLookupRedirect(@RequestParam(required = false) String phone) {
        if (phone == null || phone.isBlank()) {
            return "redirect:/order/cart";
        }
        return "redirect:/order/cart?phone=" + URLEncoder.encode(phone, StandardCharsets.UTF_8);
    }

    @GetMapping("/cart")
    public String cartGet(@RequestParam(required = false) String phone, Model model) {
        populateLookup(phone, model);
        return "/kiosk/cart";
    }

    @PostMapping("/cart")
    public String cartPost(@RequestParam(required = false) String phone, Model model) {
        populateLookup(phone, model);
        return "/kiosk/cart";
    }

    private void populateLookup(String phone, Model model) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        model.addAttribute("prefillPhone", phone);

        String digits = phone.replaceAll("\\D", "");
        if (digits.length() < 10 || digits.length() > 11) {
            model.addAttribute("lookupError", "올바른 휴대폰 번호(10~11자리)를 입력해 주세요.");
            return;
        }

        Member member = memberService.findByPhone(digits);
        if (member == null) {
            model.addAttribute("lookupError", "등록된 회원이 없습니다. 휴대폰 번호를 확인해 주세요.");
            return;
        }

        String formattedPhone = formatPhoneDisplay(digits);
        model.addAttribute("lookupPhone", formattedPhone);
        String displayName = (member.getName() != null && !member.getName().isBlank())
                ? member.getName()
                : formattedPhone;
        model.addAttribute("lookupMemberName", displayName);
        model.addAttribute("lookupPoints", member.getPoints());

        List<Coupon> raw = couponRepository.findByMemberIdOrderByCreatedAtDesc(member.getId());
        List<CouponLookupRow> coupons = raw.stream()
                .filter(c -> !c.isUsed())
                .map(this::toLookupRow)
                .toList();
        model.addAttribute("lookupCoupons", coupons);
    }

    private CouponLookupRow toLookupRow(Coupon c) {
        String benefit = c.getDiscountType() == Coupon.DiscountType.PERCENT
                ? c.getDiscountValue() + "%"
                : String.format("%,d원", c.getDiscountValue());
        return new CouponLookupRow(
                c.getCode(),
                c.getName(),
                benefit,
                c.getDiscountType().name(),
                c.getDiscountValue()
        );
    }

    private static String formatPhoneDisplay(String digits) {
        if (digits.length() == 10) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        }
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }
        return digits;
    }

    @PostMapping("/payment")
    public String payment() {
        return "/kiosk/payment";
    }

    @PostMapping("/pay")
    public String pay() {
        return "/kiosk/complete";
    }
}
