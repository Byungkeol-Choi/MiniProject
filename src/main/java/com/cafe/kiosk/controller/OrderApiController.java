package com.cafe.kiosk.controller;

import com.cafe.kiosk.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
