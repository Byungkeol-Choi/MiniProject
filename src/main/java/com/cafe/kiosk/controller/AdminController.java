package com.cafe.kiosk.controller;

import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.domain.Orders;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.repository.MemberRepository;
import com.cafe.kiosk.repository.OrdersRepository;
import com.cafe.kiosk.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminDashboardService adminDashboardService;
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
        //기존 로직이 너무 복잡하여 AdminDashboardService에서 처리하도록 변경
        adminDashboardService.addDashboardAttributes(model, authentication.getName());
        return "/admin/dashboard";
    }

    @GetMapping("/menus")
    public String adminMenus(Model model) {
        return "/admin/menus";
    }

    @GetMapping("/orders")
    public String adminOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String status,
            Model model) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        String normalized = status != null ? status.trim() : "";
        Page<Orders> orders;
        String statusFilter;
        if (normalized.isEmpty()) {
            orders = ordersRepository.findPageWithLines(pageable);
            statusFilter = "";
        } else {
            try {
                Orders.Status st = Orders.Status.valueOf(normalized);
                orders = ordersRepository.findByStatus(st, pageable);
                statusFilter = normalized;
            } catch (IllegalArgumentException ex) {
                orders = ordersRepository.findPageWithLines(pageable);
                statusFilter = "";
            }
        }

        model.addAttribute("orders", orders);
        model.addAttribute("statusFilter", statusFilter);
        return "/admin/orders";
    }

    @GetMapping("/members")
    public String adminMembers(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Member> members = memberRepository.findAll(pageable);

        model.addAttribute("members", members);
        model.addAttribute("totalMembers", memberRepository.count());
        model.addAttribute("couponCounts",
                members.map(m -> couponRepository.countByMemberIdAndUsedFalse(m.getId())).getContent());

        return "/admin/members";
    }
}
