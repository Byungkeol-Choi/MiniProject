//package com.cafe.kiosk.config;
//
//import com.cafe.kiosk.domain.Member;
//import com.cafe.kiosk.repository.MemberRepository;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class DataInitializer implements ApplicationRunner {
//
//    private final MemberRepository memberRepository;
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    @Override
//    @Transactional
//    public void run(ApplicationArguments args) {
//        // Supabase connection pooler(PgBouncer) 환경에서는 Hibernate ddl-auto: update가
//        // DDL을 실행하지 못하는 경우가 있으므로 누락 컬럼을 직접 추가
//        if (true) return;
//        entityManager.createNativeQuery(
//                "ALTER TABLE coupon ADD COLUMN IF NOT EXISTS name VARCHAR(100) NOT NULL DEFAULT ''"
//        ).executeUpdate();
//
//        // 매 서버 시작 시 member / coupon 전부 비우고 ID 시퀀스까지 초기화 (PostgreSQL)
//        entityManager.createNativeQuery(
//                "TRUNCATE TABLE coupon, member RESTART IDENTITY CASCADE"
//        ).executeUpdate();
//
//        List<Member> samples = List.of(
//                Member.builder().phone("01011112222").name("홍길동").points(1000).build(),
//                Member.builder().phone("01033334444").name("김민지").points(1000).build(),
//                Member.builder().phone("01055556666").name("이준혁").points(1000).build(),
//                Member.builder().phone("01077778888").name("박서연").points(1000).build(),
//                Member.builder().phone("01099990000").name("최동훈").points(1000).build()
//        );
//
//        memberRepository.saveAll(samples);
//        log.info("[DataInitializer] DB 초기화 완료 (member·coupon TRUNCATE + 샘플 회원 5명, 포인트 각 1000).");
//    }
//}
