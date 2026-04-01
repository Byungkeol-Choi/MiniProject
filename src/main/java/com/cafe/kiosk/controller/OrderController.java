package com.cafe.kiosk.controller;

import com.cafe.kiosk.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final CouponService couponService;

    @GetMapping("/cart")
    public String cartGet() {
        return "kiosk/cart";
    }

    @PostMapping("/cart")
    public String order() {
        return "kiosk/cart";
    }

    @PostMapping("/payment")
    public String payment(
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false, defaultValue = "0") int couponDiscount,
            @RequestParam(required = false, defaultValue = "0") int usePoints,
            @RequestParam(required = false, defaultValue = "27500") int finalAmount,
            Model model) {
        populatePaymentModel(model, couponCode, couponDiscount, usePoints, finalAmount, null);
        return "kiosk/payment";
    }

    @PostMapping("/pay")
    public String pay(
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false, defaultValue = "0") int couponDiscount,
            @RequestParam(required = false, defaultValue = "0") int usePoints,
            @RequestParam(required = false, defaultValue = "27500") int finalAmount,
            @RequestParam(required = false) String paymentMethod,
            Model model) {
        try {
            if (StringUtils.hasText(couponCode)) {
                couponService.redeemCouponByCode(couponCode.trim());
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            populatePaymentModel(model, couponCode, couponDiscount, usePoints, finalAmount, e.getMessage());
            return "kiosk/payment";
        }
        return "kiosk/complete";
    }

    private void populatePaymentModel(Model model, String couponCode, int couponDiscount,
                                      int usePoints, int finalAmount, String errorMessage) {
        int discountAmount = couponDiscount + usePoints;
        int totalAmount = finalAmount + discountAmount;
        model.addAttribute("couponCode", couponCode != null ? couponCode : "");
        model.addAttribute("couponDiscount", couponDiscount);
        model.addAttribute("usePoints", usePoints);
        // Thymeleaf에서 Map + ?. 조합이 깨질 수 있어 스칼라로 전달
        model.addAttribute("summaryTotalAmount", totalAmount);
        model.addAttribute("summaryDiscountAmount", discountAmount);
        model.addAttribute("summaryFinalAmount", finalAmount);
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
    }
}
