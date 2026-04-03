package com.cafe.kiosk.controller;

import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.domain.Orders;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.repository.MemberRepository;
import com.cafe.kiosk.repository.OrdersRepository;
import com.cafe.kiosk.service.AdminDashboardService;
import com.cafe.kiosk.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminDashboardService adminDashboardService;
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final OrdersRepository ordersRepository;
    private final OrderService orderService;

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
        LocalDate targetDate = statsDate();
        long receivedCount = ordersRepository.countByStatusForDate(targetDate, "RECEIVED");

        model.addAttribute("orders", orders);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("receivedCount", receivedCount);

        System.out.println("receivedCount" + receivedCount);
        return "/admin/orders";
    }

    public record OrderStatusPatchRequest(String status) {}
    @PatchMapping("/orders/{id}/status")
    @ResponseBody
    public ResponseEntity<?> patchOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusPatchRequest body) {
        if (body == null || body.status() == null || body.status().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "status가 필요합니다."));
        }
        try {
            Orders.Status st = Orders.Status.valueOf(body.status().trim());
            orderService.updateOrderStatus(id, st);
            return ResponseEntity.ok(Map.of("ok", true, "status", st.name()));
        } catch (IllegalArgumentException ex) {
            // valueOf 실패 또는 서비스에서 "주문 없음" 등으로 같은 예외를 쓰는 경우 구분하고 싶다면 커스텀 예외로 나누는 편이 좋음
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
    
    @GetMapping("/members")
    // @RequestParam(defaultValue = "0") int page: URL의 쿼리 스트링(예: /members?page=1)에서 page 파라미터 값을 추출합니다.
    public String adminMembers(@RequestParam(defaultValue = "0") int page,
                               Model model) {

        // id 오름차순 정렬 (1번부터 보이도록)
        // 요청 (Request) 몇 페이지를, 몇 개씩 볼지 정하는 주문서
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "id"));
        // 응답 (Response) 조회된 데이터 리스트와 전체 통계가 담긴 결과물
        Page<Member> members = memberRepository.findAll(pageable);

        model.addAttribute("members", members);
        model.addAttribute("totalMembers", memberRepository.count());
        model.addAttribute("couponCounts",
                members.map(m -> couponRepository.countByMemberIdAndUsedFalse(m.getId())).getContent());

        return "/admin/members";
    }
    private LocalDate statsDate() {
        return LocalDate.now();
//         return LocalDate.of(2026, 4, 1);
    }
}
