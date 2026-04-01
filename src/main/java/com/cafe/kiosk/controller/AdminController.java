package com.cafe.kiosk.controller;

import com.cafe.kiosk.domain.AdminMembers;
import com.cafe.kiosk.domain.Member;
import com.cafe.kiosk.repository.AdminMemberRepo;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private final AdminMemberRepo adminMemberRepo;
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;

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

    /**
     * 관리자 회원 관리 화면
     * Supabase(PostgreSQL)의 member 테이블 데이터를 페이지네이션해서 조회.
     */
    @GetMapping("/admin/members")
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


}//class
