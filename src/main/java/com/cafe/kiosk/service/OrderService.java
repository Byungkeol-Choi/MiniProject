package com.cafe.kiosk.service;

import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.domain.Menu;
import com.cafe.kiosk.domain.OrderItem;
import com.cafe.kiosk.domain.Orders;
import com.cafe.kiosk.dto.CartItemDto;
import com.cafe.kiosk.repository.MemberRepository;
import com.cafe.kiosk.repository.MenuRepository;
import com.cafe.kiosk.repository.OrderItemRepository;
import com.cafe.kiosk.repository.OrdersRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public Map<String, Object> updateCartQty(Long id, Map<String, Integer> request, HttpSession session) {
        List<CartItemDto> cartItem = (List<CartItemDto>) session.getAttribute("items");

        CartItemDto cartItemDto = cartItem.stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        int delta = request.get("delta");
        int sum = cartItemDto.getQuantity() + delta;

        if (sum <= 0) {
            cartItem.remove(cartItemDto);

            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("removed", true);
            return result;
        }

        cartItemDto.setQuantity(sum);

        Map<String, Object> result = new HashMap<>();
        result.put("id", cartItemDto.getId());
        result.put("price", cartItemDto.getPrice());
        result.put("quantity", cartItemDto.getQuantity());
        result.put("removed", false);

        return  result;
    }

    @Transactional
    public Map<String, Object> deleteCartQty(Long id, HttpSession session) {
        List<CartItemDto> cartItem = (List<CartItemDto>) session.getAttribute("items");

        CartItemDto cartItemDto = cartItem.stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        Map<String, Object> result = new HashMap<>();
        result.put("id", cartItemDto.getId());

        cartItem.removeIf(i -> i.getId().equals(id));

        return result;
    }

    @Transactional
    public void update(String paymentMethod, Integer finalAmount, HttpSession session) {
//        Long id = (Long) session.getAttribute("memberId"); // 회원 조회안하고 결제시 여기서 id에 0값이 들어감.
//        Member member = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("비회원 주문")); // 비회원 주문 에러 객체가 터져서 위로올라감.
//        Integer couponDiscount = (Integer) session.getAttribute("couponDiscount");
        // 수정 후
        Long id = (Long) session.getAttribute("memberId"); // 비회원 주문 시 JS 기본값 0(cart.html value=0), 세션 만료 시 null
        Member member = (id != null && id != 0)
                ? memberRepository.findById(id).orElse(null)
                : null;
        Integer couponDiscount = session.getAttribute("couponDiscount") != null
                ? (Integer) session.getAttribute("couponDiscount") : 0;

        Orders orders = Orders.builder()
                .totalAmount(finalAmount)
                .discountAmount(couponDiscount)
                .paymentMethod(paymentMethod)
                .status(Orders.Status.RECEIVED)
                .createdAt(LocalDateTime.now())
                .member(member) // 비회원 주문시(member가 null)이여도 정상적으로 db에 들어감.
                .build();
        ordersRepository.save(orders);

        List<CartItemDto> cartItemDto = (List<CartItemDto>) session.getAttribute("items");
        if (cartItemDto == null) {
            cartItemDto = new ArrayList<>();
        }
        List<Long> menuId = cartItemDto.stream().map(CartItemDto::getId).toList();
        List<Menu> menus = menuRepository.findAllById(menuId);
        List<OrderItem> orderItem = cartItemDto.stream()
                .map(item -> {
                    Menu menu = menus.stream()
                            .filter(m -> m.getId().equals(item.getId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("메뉴 없음"));

                    return OrderItem.builder()
                            .orders(orders)
                            .menu(menu)
                            .quantity(item.getQuantity())
                            .unitPrice(item.getPrice())
                            .build();
                })
                .toList();
        orderItemRepository.saveAll(orderItem);
    }

    @Transactional
    public void complete(HttpSession session) {
    }
}