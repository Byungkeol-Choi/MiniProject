package com.cafe.kiosk.controller;

import com.cafe.kiosk.dto.CartItemDto;
import com.cafe.kiosk.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderService orderService;

    @PatchMapping("/api/cart/{id}")
    public Map<String, Object> updateCartQty(@PathVariable Long id, @RequestBody Map<String, Integer> request, HttpSession session) {
        System.out.println(id);
        System.out.println(request);
        return orderService.updateCartQty(id, request, session);
    }

    @DeleteMapping("/api/cart/{id}")
    public Map<String, Object> deleteCartQty(@PathVariable Long id, HttpSession session) {
        System.out.println(id);
        return orderService.deleteCartQty(id, session);
    }

    @GetMapping("/api/cart/session")
    public ResponseEntity<?> getCartSession(HttpSession session){
      List<CartItemDto> items = (List<CartItemDto>) session.getAttribute("items");
      if (items == null) items = new ArrayList<>();
      return ResponseEntity.ok(items);
    }
}
