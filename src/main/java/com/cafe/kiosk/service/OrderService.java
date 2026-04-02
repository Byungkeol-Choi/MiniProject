package com.cafe.kiosk.service;

import com.cafe.kiosk.dto.CartItemDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
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

//        int subtotal = cartItem.stream()
//                .mapToInt(i -> i.getPrice() * )

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
}
