package com.cafe.kiosk.controller;

import com.cafe.kiosk.dto.MemberLookupRequest;
import com.cafe.kiosk.dto.MemberLookupResponse;
import com.cafe.kiosk.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * 키오스크 회원 조회 JSON API (장바구니 모달 등).
 */
@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 전화번호로 가입 회원의 포인트·쿠폰을 조회한다. 적립/차감은 하지 않는다.
     */
    // 호출위치: src/main/resources/static/js/kiosk.js
    @PostMapping("/lookup")
    public ResponseEntity<?> lookup(@RequestBody(required = false) MemberLookupRequest request) {
        if (request == null || !StringUtils.hasText(request.phone())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "전화번호를 입력해주세요."));
        }

        try {
            Optional<MemberLookupResponse> found = memberService.lookupMemberSummary(request.phone());
            if (found.isPresent()) {
                return ResponseEntity.ok(found.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "가입되지 않은 전화번호입니다."));
        } catch (Exception e) {
            log.error("POST /api/member/lookup phone={}", request.phone(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "조회 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
        }
    }
}
