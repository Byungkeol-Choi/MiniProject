# Java 문법/어노테이션 가이드 (프로젝트 기준)

이 문서는 현재 MiniProject Java 코드에서 실제 사용 중인 문법과 어노테이션을 빠르게 이해하기 위한 참고서입니다.

---

## 1) 클래스/DI 관련 어노테이션

## `@SpringBootApplication`

- 위치: `CafeKioskApplication`
- 의미: 스프링부트 앱 시작에 필요한 핵심 설정 묶음
  - `@Configuration`
  - `@EnableAutoConfiguration`
  - `@ComponentScan`
- 효과: `main()`에서 `SpringApplication.run(...)` 호출 시 컴포넌트 스캔과 자동 설정이 활성화됨

## `@Controller`

- 위치: `MemberController`
- 의미: MVC 컨트롤러 빈 등록
- 특징: 메서드 반환 문자열을 뷰 이름으로 해석 (`"kiosk/stamp"` 등)

## `@Service`

- 위치: `MemberService`, `CouponService`
- 의미: 서비스 계층 빈 등록(비즈니스 로직 담당)
- 실무 의미: 컨트롤러/리포지토리 사이의 도메인 규칙을 한곳에 모음

## `@Configuration`

- 위치: `SecurityConfig`
- 의미: `@Bean` 메서드를 포함하는 설정 클래스

## `@Component`

- 위치: `DataInitializer`
- 의미: 일반 스프링 빈 등록
- 이 프로젝트에서는 앱 시작 시 실행되는 초기화 컴포넌트로 사용

## `@Bean`

- 위치: `SecurityConfig.filterChain`, `SecurityConfig.passwordEncoder`
- 의미: 메서드 반환 객체를 스프링 컨테이너가 관리하도록 등록

## `@RequiredArgsConstructor` (Lombok)

- 위치: 컨트롤러/서비스/초기화 클래스
- 의미: `final` 필드를 파라미터로 받는 생성자를 자동 생성
- 효과: 생성자 주입을 보일러플레이트 없이 사용

## `@Slf4j` (Lombok)

- 위치: `MemberController`, `DataInitializer`
- 의미: `log` 필드(`org.slf4j.Logger`) 자동 생성

---

## 2) 웹 요청 매핑 문법

## `@GetMapping`, `@PostMapping`

- 의미: HTTP 메서드 + 경로 매핑
- 예:
  - `@GetMapping("/")`
  - `@PostMapping("/member/stamp")`

## `@RequestParam`

- 의미: 쿼리스트링/폼 파라미터를 메서드 인자로 바인딩
- 자주 쓰는 옵션:
  - `required = false`
  - `defaultValue = "0"`

## `Model`

- 의미: 컨트롤러에서 뷰로 데이터 전달용 객체
- 사용: `model.addAttribute("key", value)`

---

## 3) 트랜잭션 관련

## `@Transactional`

- 위치: 서비스 메서드, `DataInitializer.run`
- 의미: 메서드 실행을 트랜잭션 경계로 묶음
- 효과: 실패 시 롤백, 성공 시 커밋

## `@Transactional(readOnly = true)`

- 위치: 서비스 클래스 레벨
- 의미: 기본을 읽기 전용 트랜잭션으로 설정
- 패턴: 조회 중심 메서드는 그대로 사용하고, 쓰기 메서드에서 `@Transactional` 재선언

---

## 4) JPA 엔티티 어노테이션

## `@Entity`, `@Table`

- 의미: JPA 관리 대상 클래스 및 매핑 테이블 지정
- 예:
  - `@Entity`
  - `@Table(name = "coupon")`

## `@Id`, `@GeneratedValue`

- 의미: 기본키와 PK 생성 전략 지정
- 사용 전략: `GenerationType.IDENTITY` (DB auto increment)

## `@Column`

- 의미: 컬럼 속성 제어
- 자주 쓰는 옵션:
  - `nullable = false`
  - `unique = true`
  - `length = 100`
  - `name = "created_at"`
  - `updatable = false`

## `@ManyToOne`, `@JoinColumn`

- 의미: 다대일 연관관계와 FK 컬럼 지정
- 예: `Coupon.member` -> `member_id`
- `fetch = FetchType.LAZY`: 실제 필요 시점에 연관 엔티티 로딩

## `@Enumerated(EnumType.STRING)`

- 의미: enum을 DB에 문자열로 저장
- 장점: enum 순서 변경에 안전(ordinal 저장 방식보다 유지보수 유리)

## `@PrePersist`

- 의미: 엔티티 INSERT 직전 실행되는 콜백 메서드
- 이 프로젝트 사용 예: `createdAt` 자동 세팅

---

## 5) Spring Data JPA 리포지토리 문법

## `JpaRepository<T, ID>`

- 의미: 기본 CRUD + 페이징/정렬 기능 제공
- 프로젝트 예:
  - `MemberRepository extends JpaRepository<Member, Long>`
  - `CouponRepository extends JpaRepository<Coupon, Long>`

## 파생 쿼리 메서드 (메서드 이름 기반)

- 예:
  - `findByCodeAndUsedFalse`
  - `existsByCode`
  - `findByMemberIdOrderByCreatedAtDesc`
- 의미: 메서드명 규칙으로 SQL/JPA 쿼리 자동 생성

## `@Query` + `@Param`

- 위치: `MemberRepository.findByPhoneNormalized`
- 의미: 커스텀 쿼리 직접 작성
- 이 프로젝트는 `nativeQuery = true`로 하이픈 제거 비교 수행

---

## 6) Lombok 데이터 클래스 문법

엔티티에서 아래 어노테이션을 함께 사용:

- `@Getter`, `@Setter`: 접근자 자동 생성
- `@NoArgsConstructor`, `@AllArgsConstructor`: 기본/전체 생성자 자동 생성
- `@Builder`: 빌더 패턴 생성
- `@Builder.Default`: 빌더 사용 시 기본값 유지 (`points = 0`, `used = false`)

---

## 7) Java 문법 포인트 (코드에서 자주 보이는 것)

## Optional 처리

- `Optional<T>` 반환 후:
  - `orElse(null)`
  - `orElseThrow(() -> new IllegalArgumentException(...))`

## while 반복 정책 처리

- 포인트가 임계치(`3000`)를 넘는 동안 쿠폰 반복 발급:
  - `while (member.getPoints() >= COUPON_THRESHOLD) { ... }`

## 삼항 연산자

- 표시 이름 결정:
  - `condition ? valueIfTrue : valueIfFalse`

## 문자열 정규화

- 전화번호 숫자만 추출:
  - `phone.replaceAll("[^0-9]", "")`

---

## 8) 예외 처리 패턴

- 비즈니스 검증 실패:
  - `IllegalArgumentException` (예: 회원 없음, 쿠폰 없음)
  - `IllegalStateException` (예: 만료 쿠폰, 포인트 부족)
- 컨트롤러에서 `try-catch`로 사용자 메시지 분기 처리

---

## 9) 함께 보면 좋은 문서

- 엔드포인트 명세: `docs/API_ENDPOINTS.md`
- Java 코드 상세 가이드: `docs/JAVA_CODE_GUIDE.md`
- 실행 흐름도: `docs/ENDPOINT_FLOW_MERMAID.md`
- 엔티티 ERD: `docs/ENTITY_ERD.md`
