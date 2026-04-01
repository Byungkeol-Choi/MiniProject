package com.cafe.kiosk.controller;

import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.repository.AdminMemberRepo;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.repository.MemberRepository;
import com.cafe.kiosk.repository.OrdersRepository;
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

    @GetMapping("/admin/login")
    public String adminLogin() {
                return "/admin/login";
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
    @GetMapping("/members")
    public String adminMembers(@RequestParam(defaultValue = "0") int page,
                               Model model) {

        // id 오름차순 정렬 (1번부터 보이도록)
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Member> members = memberRepository.findAll(pageable);

        model.addAttribute("members", members);
        model.addAttribute("totalMembers", memberRepository.count());
        // 회원별 미사용 쿠폰 수 (Supabase coupon 테이블 기준)
        model.addAttribute("couponCounts",
                members.map(m -> couponRepository.countByMemberIdAndUsedFalse(m.getId())).getContent());

        return "/admin/members";
    }
}
