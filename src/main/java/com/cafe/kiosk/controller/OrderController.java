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

        // System.out.println("items : " + cart.getItems());

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

        int usePoint = (int) session.getAttribute("usePoints");
        int couponDiscount = (int) session.getAttribute("couponDiscount");
        int finalAmount = totalAmount - (usePoint > 0 ? usePoint : couponDiscount);


        model.addAttribute("cartItems", cartItemDto);
        model.addAttribute("summaryTotalAmount", totalAmount);
        model.addAttribute("summaryDiscountAmount", usePoint);
        model.addAttribute("summaryFinalAmount", finalAmount);

        return "kiosk/payment";
    }

    @PostMapping("/order/payment")
    public String payment(HttpSession session, CartItemSessionDto sessionDto) {
        session.setAttribute("couponCode", sessionDto.getCouponCode());
        session.setAttribute("couponDiscount", sessionDto.getCouponDiscount());
        session.setAttribute("usePoints", sessionDto.getUsePoints());

        return "redirect:/order/payment";
    }

    @PostMapping("/order/pay")
    public String pay() {
        return "kiosk/complete";
    }
}
