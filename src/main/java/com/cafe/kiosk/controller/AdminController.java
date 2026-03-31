package com.cafe.kiosk.controller;

import com.cafe.kiosk.domain.AdminMembers;
import com.cafe.kiosk.repository.AdminMemberRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private final AdminMemberRepo adminMemberRepo;

    //    테스트용
//    @GetMapping("/")
//    public String index(Model model) {
//        List<AdminMembers> list = adminMemberRepo.findAll();
//        System.out.println("list size: " + list.size());
//        System.out.println(list.toString());
//        return "/kiosk/index";
//    }

    //    테스트용
    @GetMapping("/admin")
    public String adminDashboard(Model model) {

        List<AdminMembers> list = adminMemberRepo.findAll();
        System.out.println("list size: " + list.size());
        System.out.println(list.toString());
        return "/admin/dashboard";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
                return "/admin/login";
    }

}
