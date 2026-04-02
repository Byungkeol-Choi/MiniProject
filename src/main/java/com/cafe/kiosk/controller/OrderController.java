package com.cafe.kiosk.controller;

import com.cafe.kiosk.dto.CartItemDto;
import com.cafe.kiosk.dto.CartItemSessionDto;
import com.cafe.kiosk.service.CouponService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final ObjectMapper objectMapper; //json 세션 저장 방법
    private final CouponService couponService;

    @GetMapping("/order/cart")
    public String confrim(HttpSession session, Model model) {
        List<CartItemDto> cartItemDto = (List<CartItemDto>) session.getAttribute("items");
        if (cartItemDto == null) {
            cartItemDto = new ArrayList<>();
        }

        // System.out.println(cartItemDto);

        model.addAttribute("cartItems", cartItemDto);

        return "kiosk/cart";
    }

    @PostMapping("/order/cart")
    public String order(@RequestParam("cartData") String cartData, HttpSession session) throws Exception {
        CartItemSessionDto cart = objectMapper.readValue(cartData, CartItemSessionDto.class);

        session.setAttribute("items", cart.getItems());
        session.setAttribute("memberId", cart.getMemberId());
        session.setAttribute("memberName", cart.getMemberName());
        session.setAttribute("couponCode", cart.getCouponCode());
        session.setAttribute("couponDiscount", cart.getCouponDiscount());
        session.setAttribute("usePoints", cart.getUsePoints());

        // System.out.println("items : " + cart.getItems());

        return "redirect:/order/cart";
    }

    @PostMapping("/order/payment")
    public String payment() {
        return "/kiosk/payment";
    }

    @PostMapping("/order/pay")
    public String pay(@RequestParam(required = false) String couponCode,
                      @RequestParam(defaultValue = "0") int finalAmount,
                      HttpSession session) {
        couponService.redeemCouponByCode(couponCode); // db 쿠폰 사용됨이라고 바꿈.
        session.setAttribute("finalAmount", finalAmount); // 쿠폰할인 적용된 최종 결제금액 세션에 저장.
        return "/kiosk/complete";
    }
}
