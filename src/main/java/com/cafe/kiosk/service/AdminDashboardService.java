package com.cafe.kiosk.service;

import com.cafe.kiosk.domain.Orders;
import com.cafe.kiosk.repository.CouponRepository;
import com.cafe.kiosk.repository.MemberRepository;
import com.cafe.kiosk.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final OrdersRepository ordersRepository;

    public void addDashboardAttributes(Model model, String adminName) {

        LocalDate targetDate = statsDate();
        //Dashboard - 오늘 매출
        long targetDateSales = ordersRepository.sumNetSalesForDate(targetDate);

        //Dashboard - 오늘 주문 현황 : 접수, 준비중, 완료, 취소, 총 주문
        long receivedCount = ordersRepository.countByStatusForDate(targetDate, "RECEIVED");
        long preparingCount = ordersRepository.countByStatusForDate(targetDate, "PREPARING");
        long completedCount = ordersRepository.countByStatusForDate(targetDate, "COMPLETED");
        long cancelledCount = ordersRepository.countByStatusForDate(targetDate, "CANCELLED");
        long todayOrderCount = receivedCount + preparingCount + completedCount + cancelledCount;

        //Dashboard - 총 회원 수, 오늘의 회원 수
        long totalMembers = memberRepository.count();
        long todayMemberCount = memberRepository.countByMemberForDate(targetDate);

        //Dashboard - 발행쿠폰 수, 유효한 미사용 쿠폰 수(만료일 경과 분 제외)
        long couponCount = couponRepository.count();
        long unusedCouponCount = couponRepository.countByUsedFalse();

        //Dashboard - 최근 주문 리스트
        Pageable recent = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Orders> recentOrders = ordersRepository.findAll(recent).getContent();

        model.addAttribute("adminName", adminName);

        model.addAttribute("todaySales", targetDateSales);
        model.addAttribute("todayOrderCount", todayOrderCount);
        model.addAttribute("memberCount", totalMembers);
        model.addAttribute("couponCount", couponCount);
        model.addAttribute("unusedCouponCount", unusedCouponCount);

        model.addAttribute("receivedCount", receivedCount);
        model.addAttribute("preparingCount", preparingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);

        model.addAttribute("todayMemberCount", todayMemberCount);

        model.addAttribute("recentOrders", recentOrders);

        // 데이터 확인용 추후 삭제 CBK
        for (Orders o : recentOrders) {
            System.out.println("recentOrder id=" + o.getId()
                    + ", status=" + o.getStatus()
                    + ", totalAmount=" + o.getTotalAmount()
                    + ", createdAt=" + o.getCreatedAt());
        }
    }

    /** 집계 기준일. 테스트 시 고정일로 바꿀 수 있습니다. */
    private LocalDate statsDate() {
        return LocalDate.now();
//         return LocalDate.of(2026, 4, 1);
    }
}
