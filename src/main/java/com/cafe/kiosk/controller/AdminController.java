package com.cafe.kiosk.controller;

import com.cafe.kiosk.repository.AdminMemberRepo;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.repository.MemberRepository;
import com.cafe.kiosk.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminMemberRepo adminMemberRepo;
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final OrdersRepository ordersRepository;


    @GetMapping("/login")
    public String adminLogin(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && AuthorityUtils.authorityListToSet(authentication.getAuthorities()).contains("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        }
                return "/admin/login";
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Authentication authentication) {

        long totalMembers = memberRepository.count();
        long couponCount = couponRepository.count();

        model.addAttribute("memberCount", totalMembers);
        model.addAttribute("adminName", authentication.getName()); 
        model.addAttribute("couponCount", couponCount);
        // List<AdminMembers> list = adminMemberRepo.findAll();
        // System.out.println("dashboard page");
        // System.out.println("list size: " + list.size());
        // System.out.println(list.toString());
        System.out.println("adminName: " + authentication.getName());

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
