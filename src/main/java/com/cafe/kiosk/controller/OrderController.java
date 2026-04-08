package com.cafe.kiosk.controller;

import com.cafe.kiosk.dto.CartItemDto;
import com.cafe.kiosk.dto.CartItemSessionDto;
import com.cafe.kiosk.service.CouponService;
import com.cafe.kiosk.service.MemberService;
import com.cafe.kiosk.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderController {
    private final ObjectMapper objectMapper; //json 세션 저장 방법
    private final CouponService couponService;
    private final OrderService orderService;
    private final MemberService memberService;

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

        // 이전 버튼으로 돌아올 때 쿠폰·회원·포인트 정보 복원을 위해 세션값을 모델에 담는다
        model.addAttribute("sessionCouponCode", session.getAttribute("couponCode"));
        model.addAttribute("sessionCouponDiscount", session.getAttribute("couponDiscount"));
        model.addAttribute("sessionMemberId", session.getAttribute("memberId"));
        model.addAttribute("sessionMemberName", session.getAttribute("memberName"));
        model.addAttribute("sessionUsePoints", session.getAttribute("usePoints"));

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

        Integer usePoints = (Integer) session.getAttribute("usePoints");
        int pointsDiscount = (usePoints != null && usePoints > 0) ? usePoints : 0;
        int finalAmount = totalAmount - couponDiscount - pointsDiscount;

        model.addAttribute("cartItems", cartItemDto);
        model.addAttribute("summaryTotalAmount", totalAmount);
        model.addAttribute("summaryDiscountAmount", couponDiscount);
        model.addAttribute("summaryPointsDiscount", pointsDiscount);
        model.addAttribute("summaryFinalAmount", finalAmount);

        return "kiosk/payment";
    }

    @PostMapping("/order/payment")
    public String payment(@RequestParam(required = false) Long memberId,
                          @RequestParam(required = false) String memberName,
                          @RequestParam(required = false) String couponCode,
                          @RequestParam(defaultValue = "0") int couponDiscount,
                          @RequestParam(defaultValue = "0") int usePoints,
                          HttpSession session) {
        System.out.println("회원명"+memberName);
        System.out.println("회원명"+memberId);

        if (memberName != null) session.setAttribute("memberName", memberName);
        if (memberId != null) session.setAttribute("memberId", memberId);
        if (couponCode != null && !couponCode.isEmpty()) {
            session.setAttribute("couponCode", couponCode);
            session.setAttribute("couponDiscount", couponDiscount);
        }
        if (usePoints > 0) session.setAttribute("usePoints", usePoints);

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

        Long memberId = (Long) session.getAttribute("memberId");
        Integer usePoints = (Integer) session.getAttribute("usePoints");
        if (memberId != null && memberId != 0 && usePoints != null && usePoints > 0) {
            memberService.deductPoints(memberId, usePoints);
        }

        // session.setAttribute("finalAmount", finalAmount); // 쿠폰할인 적용된 최종 결제금액 세션에 저장.
        orderService.update(paymentMethod, finalAmount, session);

        return "redirect:/order/complete";
    }
}
