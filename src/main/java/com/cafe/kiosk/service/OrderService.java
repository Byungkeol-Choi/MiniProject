package com.cafe.kiosk.service;

import com.cafe.kiosk.domain.Menu;
import com.cafe.kiosk.domain.OrderItem;
import com.cafe.kiosk.domain.Orders;
import com.cafe.kiosk.dto.CartItemDto;
import com.cafe.kiosk.repository.MenuRepository;
import com.cafe.kiosk.repository.OrdersRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final MenuRepository menuRepository;
    private final OrdersRepository ordersRepository;

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

        int subtotal = cartItem.stream()
                .mapToInt(i -> i.getPrice() * i.getQuantity())
                .sum();

        Map<String, Object> result = new HashMap<>();
        result.put("id", cartItemDto.getId());
        result.put("price", cartItemDto.getPrice());
        result.put("quantity", cartItemDto.getQuantity());
        result.put("removed", false);
        result.put("subtotal", subtotal);

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
    public void saveOrder(List<CartItemDto> items, int finalAmount, int discountAmount, String paymentMethod) {
        Orders orders = Orders.builder()
                .totalAmount(finalAmount)
                .discountAmount(discountAmount)
                .paymentMethod(paymentMethod)
                .status(Orders.Status.RECEIVED)
                .createdAt(LocalDateTime.now())
                .build();

        ordersRepository.save(orders);

        for (CartItemDto item : items) {
            Menu menu = menuRepository.findById(item.getId())
                    .orElseThrow(() -> new IllegalArgumentException("메뉴 없음: " + item.getId()));
            orders.getOrderItem().add(new OrderItem(orders, menu, item.getQuantity(), item.getPrice()));
        }
    }
}
