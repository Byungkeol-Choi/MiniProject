/**
 * 루트에서: npm install pptxgenjs && node doc/generate-pptx.cjs
 */
const path = require("path");
const PptxGenJS = require("pptxgenjs");

const outPath = path.join(__dirname, "Kiosk_Project_Presentation.pptx");
const pptx = new PptxGenJS();
pptx.layout = "LAYOUT_WIDE";
pptx.title = "브런치 카페 키오스크";

const BG = "1A1410";
const TITLE = "D4A574";
const TXT = "F5EBE0";

function addSlide(title, lines) {
  const s = pptx.addSlide();
  s.background = { color: BG };
  s.addText(title, { x: 0.5, y: 0.4, w: 12.5, h: 0.7, fontSize: 28, bold: true, color: TITLE, fontFace: "Malgun Gothic" });
  const text = Array.isArray(lines) ? lines.map((t) => `• ${t}`).join("\n\n") : lines;
  s.addText(text, {
    x: 0.5,
    y: 1.2,
    w: 12.5,
    h: 5.8,
    fontSize: 18,
    color: TXT,
    fontFace: "Malgun Gothic",
    lineSpacingMultiple: 1.2,
  });
}

addSlide("브런치 카페 키오스크", ["팀장 최병걸 · 팀원 유재혁 · 정소희 · 이기문", "Spring Boot 4.0.3 · Supabase"]);
addSlide("목차", ["개요 · 환경 · DB · 구조 · 키오스크 · 관리자 · 분장 · Git · 시연 · AAR"]);
addSlide("개요", ["키오스크 주문·결제", "관리자 운영", "FOOD/DRINK 메뉴"]);
addSlide("개발 환경", ["Java 21", "Spring Boot · Security · JPA", "Thymeleaf · JS", "PostgreSQL / Supabase"]);
addSlide("DB", ["member, menu, orders, order_item, coupon, admin"]);
addSlide("Git 브랜치", ["main → dev", "menu/order/member/admin 각 2 ~ 4.1"]);
addSlide("시연", ["http://localhost:8080", "GitHub Issues"]);

pptx.writeFile({ fileName: outPath }).then(() => console.log("생성:", outPath));
