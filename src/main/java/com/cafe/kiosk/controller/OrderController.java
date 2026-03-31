package com.cafe.kiosk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/order")
public class OrderController {
    @PostMapping("/cart")
    public String order() {
        return "/kiosk/cart";
    }

    @PostMapping("/payment")
    public String payment() {
        return "/kiosk/payment";
    }

    @PostMapping("/pay")
    public String pay() {
        return "/kiosk/complete";
    }
}
