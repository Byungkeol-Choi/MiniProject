package com.cafe.kiosk.controller;

import com.cafe.kiosk.dto.CartItemDto;
import com.cafe.kiosk.dto.CartItemSessionDto;
import com.cafe.kiosk.service.CouponService;
import com.cafe.kiosk.service.OrderService;
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
    private final OrderService orderService;

    @GetMapping("/order/cart")
    public String confrim(HttpSession session, Model model) {
        List<CartItemDto> cartItemDto = (List<CartItemDto>) session.getAttribute("items");
        if (cartItemDto == null) {
            cartItemDto = new ArrayList<>();
        }

        int subtotal = cartItemDto.stream()
                        .mapToInt((item)->item.getPrice() * item.getQuantity())
                                .sum();

        model.addAttribute("cartItems", cartItemDto);
        model.addAttribute("subtotal", subtotal);

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

        System.out.println("items : " + cart.getItems());
        System.out.println("memberId : " + cart.getMemberId());

        return "redirect:/order/cart";
    }

    @GetMapping("/order/payment")
    public String payment(HttpSession session, Model model) {
        List<CartItemDto> cartItemDto = (List<CartItemDto>) session.getAttribute("items");
        if (cartItemDto == null) {
            cartItemDto = new ArrayList<>();
        }

        int totalAmount = cartItemDto.stream()
                .mapToInt((item)->item.getPrice() * item.getQuantity())
                .sum();

        String couponCode = (String) session.getAttribute("couponCode");
        int couponDiscount = 0;
        if (couponCode != null && !couponCode.isEmpty()) {
            couponDiscount = (int) session.getAttribute("couponDiscount");
        }

        int finalAmount = totalAmount - couponDiscount;


        model.addAttribute("cartItems", cartItemDto);
        model.addAttribute("summaryTotalAmount", totalAmount);
        model.addAttribute("summaryDiscountAmount", couponDiscount);
        model.addAttribute("summaryFinalAmount", finalAmount);

        return "kiosk/payment";
    }

    @PostMapping("/order/payment")
    public String payment(@RequestParam(required = false) Long memberId,
                          @RequestParam(required = false) String memberName,
                          @RequestParam(required = false) String couponCode,
                          @RequestParam(defaultValue = "0") int couponDiscount,
                          HttpSession session) {
        System.out.println("회원명"+memberName);
        System.out.println("회원명"+memberId);

        if (memberName != null) session.setAttribute("memberName", memberName);
        if (memberId != null) session.setAttribute("memberId", memberId);
        if (couponCode != null && !couponCode.isEmpty()) {
            session.setAttribute("couponCode", couponCode);
            session.setAttribute("couponDiscount", couponDiscount);
        }

        return "redirect:/order/payment";
    }

    @GetMapping("/order/complete")
    public String complete(HttpSession session, Model model) {
        Long orderId = (Long) session.getAttribute("orderId");
        int finalAmount = (int) session.getAttribute("finalAmount");
        System.out.println(orderId+","+finalAmount);
        model.addAttribute("orderNo", orderId);
        model.addAttribute("finalAmount", finalAmount);
        return "kiosk/complete";
    }

    @PostMapping("/order/pay")
    public String pay(@RequestParam(required = false) String paymentMethod,
                      @RequestParam(defaultValue = "0") int finalAmount,
                      HttpSession session) {
        String couponCode = (String) session.getAttribute("couponCode");
        if (couponCode != null && !couponCode.isEmpty()) {
            couponService.redeemCouponByCode(couponCode); // db 쿠폰 사용됨이라고 바꿈.
        }

        // session.setAttribute("finalAmount", finalAmount); // 쿠폰할인 적용된 최종 결제금액 세션에 저장.
        orderService.update(paymentMethod, finalAmount, session);

        return "redirect:/order/complete";
    }
}
