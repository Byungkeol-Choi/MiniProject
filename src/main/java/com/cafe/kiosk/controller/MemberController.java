package com.cafe.kiosk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {

    /**
     * GET /member/stamp
     * 스탬프 적립 페이지를 반환한다.
     * 포인트 적립·쿠폰 조회는 /api/member/stamp-info, /api/member/stamp REST API로 처리한다.
     */
    @GetMapping("/member/stamp")
    public String stampForm() {
        return "kiosk/stamp";
    }
}
