package com.cafe.kiosk.controller;

import com.cafe.kiosk.domain.AdminMembers;
import com.cafe.kiosk.repository.AdminMemberRepo;
import com.cafe.kiosk.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminMemberRepo adminMemberRepo;
    private final MemberRepository memberRepository;

    @GetMapping("/login")
    public String adminLogin() {
                return "/admin/login";
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {

        List<AdminMembers> list = adminMemberRepo.findAll();
        System.out.println("dashboard page");
        System.out.println("list size: " + list.size());
        System.out.println(list.toString());
        return "/admin/dashboard";
    }

    @GetMapping("/members")
    public String adminMembers(Model model) {
        System.out.println("member page");
//        List<Member> list = memberRepository.findAll();
//        System.out.println("list size: " + list.size());
        return "/admin/members";
    }

    @GetMapping("/menus")
    public String adminMenus(Model model) {
        System.out.println("menus page");
        return "/admin/menus";
    }

    @GetMapping("/orders")
    public String adminOrders(Model model) {
        System.out.println("orders page");
        return "/admin/orders";
    }
}
