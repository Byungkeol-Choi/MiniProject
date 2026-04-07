package com.cafe.kiosk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
        // 메소드 반환 객체를 빈으로 등록한다.
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //csrf보안 설정을 비활성화(개발편의시)/활성화(기본)
//                .csrf( (auth) -> auth.disable())
                //csrf 활성화(기본)
                // CSRF 보안 방식 2가지
                //1. HttpSession(기본) : 서버에 인증정보 저장한다.
                //2. CookieToken 방식 : JS 기반 앱 제작시 쿠키에 CsrfToken 저장해야됨.

                // CSRF 설정 : 람다배개변수 타입은 생략 가능함. 타입 추정으로
                .csrf((csrf) -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // JSON API 엔드포인트. fetch 호출 시 CSRF 토큰 전송 생략을 허용한다.
                        .ignoringRequestMatchers("/admin/api/**", "/order/cart", "/order/payment", "/order/pay")
                        // 키오스크: 메인(/)을 오래 연 채 두거나 서버 재기동 후에도 Thymeleaf가 넣어 준 hidden CSRF가
                        // 세션과 어긋나 POST가 403이 되는 경우가 있어, 동일 출처 키오스크 주문 흐름만 예외 처리.
                )
                //HTTP 요청에 대한 보안을 설정한다. Security 6버전.
                //경로별 인가 설정
                // authz : Authorization(인가), authn : Authentication(인증)
                // AuthorizationManagerRequestMatcherRegistry 타입이다.
                .authorizeHttpRequests((authz) -> authz
                                // permitAll() : 모두에게 허용
                                // authenticated() : 인증된 사용자에게 허용
                                // hasRole("ADMIN") : ADMIN 권한을 가진 사용자에게 허용
                                .requestMatchers("/", "/admin/login").permitAll()
                                .requestMatchers("/kiosk/**").permitAll()
                                // .requestMatchers("/admin").hasAuthority("ROLE_ADMIN")    //403 Forbidden
                                .requestMatchers("/admin/**").hasRole("ADMIN")    //403 Forbidden
//                                .anyRequest().authenticated()
                                .anyRequest().permitAll()       // 개발중 임시로 설정.
                )
                //로그인 페이지 설정/액션 설정
                // FormLoginConfigurer<HttpSecurity>
                .formLogin((Login) -> Login
                                .loginPage("/admin/login")    //로그인폼 요청 URL
                                // loginAction에 대한 인증처리는 시큐리트가 다 한다. 코드 필요없다.
                                .loginProcessingUrl("/admin/login") //로그인 액션 요청 URL
                                .defaultSuccessUrl("/admin/dashboard") //로그인 성공시 리다이렉트 URL
                                //로그인 성공 커스텀 핸들러
                                .successHandler((request, response, auth) -> {
                                    System.out.println("로그인 성공했습니다.");
                                    response.sendRedirect("/admin/dashboard");
                                })
                                //로그인 실패 에러페이지
                                .failureUrl("/admin/login?error")
                                .permitAll()
                )
                //로그아웃 URL/세션 설정
                //LogoutConfigurer<HttpSecurity>
                .logout((logout) -> logout
                                .logoutUrl("/admin/logout") //Post방식 추천(보안)
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true) //세션 객체 해제
                                .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
