package com.cafe.kiosk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
//    테스트용
    @GetMapping("/")
    public String index() {
//        return "/fragments/layout";
        return "/kiosk/index";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
//        return "/fragments/layout";
                return "/admin/login";
    }
}
