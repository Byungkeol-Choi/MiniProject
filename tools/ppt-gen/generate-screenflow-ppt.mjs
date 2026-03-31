import pptxgen from 'pptxgenjs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const projectRoot = path.resolve(__dirname, '..', '..');
const mockupsDir = path.join(projectRoot, 'docs', 'screen-mockups');
const outFile = path.join(projectRoot, 'docs', '브런치카페_화면흐름도.pptx');

const pptx = new pptxgen();
pptx.layout = 'LAYOUT_WIDE';
pptx.author = 'MiniProject';
pptx.title = '브런치 카페 키오스크 · 관리자 화면 흐름도';

function img(name) {
  return path.join(mockupsDir, name);
}

// ── 1. 표지
{
  const s = pptx.addSlide();
  s.addText('브런치 카페', {
    x: 0.5,
    y: 1.2,
    w: 12.33,
    h: 1,
    fontSize: 40,
    bold: true,
    color: '5C3317',
    align: 'center',
  });
  s.addText('키오스크 · 관리자 화면 흐름도', {
    x: 0.5,
    y: 2.3,
    w: 12.33,
    h: 0.6,
    fontSize: 22,
    color: '6B4226',
    align: 'center',
  });
  s.addText('docs/screen-mockups PNG 기준 목업', {
    x: 0.5,
    y: 3.2,
    w: 12.33,
    h: 0.4,
    fontSize: 14,
    color: '888888',
    align: 'center',
  });
}

// ── 2. 흐름도 요약 (텍스트)
{
  const s = pptx.addSlide();
  s.addText('키오스크 주문 흐름 (요약)', {
    x: 0.4,
    y: 0.35,
    w: 12.5,
    h: 0.5,
    fontSize: 24,
    bold: true,
    color: '5C3317',
  });
  const flow =
    '① 키오스크 메인 (메뉴·장바구니)\n' +
    '   → ② 주문 확인 (수량·쿠폰·포인트)\n' +
    '   → ③ 결제 (수단 선택)\n' +
    '   → ④ 결제 완료 (주문번호)\n' +
    '   → ⑤ 포인트 적립 (전화번호, 선택) → ① 처음으로\n\n' +
    '※ 회원: 헤더「회원 로그인」(/member/login) — 별도 화면\n' +
    '※ 관리: 각 화면 하단「관리자」→ 관리자 로그인';
  s.addText(flow, {
    x: 0.5,
    y: 1,
    w: 12,
    h: 4,
    fontSize: 16,
    color: '333333',
    valign: 'top',
  });
}

// ── 3. 한 장에 키오스크 순서 미리보기 (썸네일 + 화살표)
{
  const s = pptx.addSlide();
  s.addText('키오스크 화면 순서 (미리보기)', {
    x: 0.4,
    y: 0.25,
    w: 12.5,
    h: 0.45,
    fontSize: 20,
    bold: true,
    color: '5C3317',
  });
  const thumbs = [
    { file: 'kiosk-index.png', label: '① 메인' },
    { file: 'kiosk-cart.png', label: '② 주문확인' },
    { file: 'kiosk-payment.png', label: '③ 결제' },
    { file: 'kiosk-complete.png', label: '④ 완료' },
    { file: 'kiosk-stamp.png', label: '⑤ 적립' },
  ];
  const thumbW = 2.15;
  const thumbH = 1.2;
  const yImg = 0.95;
  const yLb = 2.25;
  let x = 0.35;
  const gap = 0.12;
  thumbs.forEach((t, i) => {
    s.addImage({
      path: img(t.file),
      x,
      y: yImg,
      w: thumbW,
      h: thumbH,
      rounding: true,
    });
    s.addText(t.label, {
      x,
      y: yLb,
      w: thumbW,
      h: 0.35,
      fontSize: 11,
      align: 'center',
      color: '333333',
    });
    if (i < thumbs.length - 1) {
      s.addShape(pptx.ShapeType.rightArrow, {
        x: x + thumbW + 0.02,
        y: yImg + thumbH / 2 - 0.12,
        w: gap + 0.08,
        h: 0.24,
        fill: { color: 'D4A96A' },
        line: { color: 'D4A96A', width: 0 },
      });
    }
    x += thumbW + gap + 0.2;
  });
  s.addText('← 완료·적립 후「처음으로」로 ① 복귀', {
    x: 0.4,
    y: 2.75,
    w: 12,
    h: 0.35,
    fontSize: 13,
    color: '666666',
  });
}

// ── 키오스크 전체 화면 (큰 이미지)
const kioskSlides = [
  { png: 'kiosk-index.png', title: '① 키오스크 메인', sub: 'index.html · 메뉴 탭 · 장바구니 · 주문하기' },
  { png: 'kiosk-cart.png', title: '② 주문 확인', sub: 'cart.html · 쿠폰·포인트 · 결제 진행' },
  { png: 'kiosk-payment.png', title: '③ 결제', sub: 'payment.html · 수단 선택 · 결제하기' },
  { png: 'kiosk-complete.png', title: '④ 결제 완료', sub: 'complete.html · 주문번호 · 포인트 적립 / 처음으로' },
  { png: 'kiosk-stamp.png', title: '⑤ 포인트 적립', sub: 'stamp.html · 전화번호 · 적립 / 건너뛰기' },
];

for (const k of kioskSlides) {
  const s = pptx.addSlide();
  s.addText(k.title, {
    x: 0.4,
    y: 0.25,
    w: 12.5,
    h: 0.45,
    fontSize: 22,
    bold: true,
    color: '5C3317',
  });
  s.addText(k.sub, {
    x: 0.4,
    y: 0.68,
    w: 12.5,
    h: 0.35,
    fontSize: 12,
    color: '666666',
  });
  s.addImage({
    path: img(k.png),
    x: 0.5,
    y: 1.05,
    w: 12.33,
    h: 5.5,
    rounding: true,
  });
}

// ── 공통 조각
{
  const s = pptx.addSlide();
  s.addText('공통 UI 조각 (fragments/layout.html)', {
    x: 0.4,
    y: 0.25,
    w: 12.5,
    h: 0.45,
    fontSize: 20,
    bold: true,
    color: '1E2A38',
  });
  s.addText('kiosk-header · admin-sidebar', {
    x: 0.4,
    y: 0.68,
    w: 12.5,
    h: 0.35,
    fontSize: 12,
    color: '666666',
  });
  s.addImage({
    path: img('fragments-layout.png'),
    x: 0.5,
    y: 1.05,
    w: 12.33,
    h: 5.5,
    rounding: true,
  });
}

// ── 관리자 흐름 요약
{
  const s = pptx.addSlide();
  s.addText('관리자 화면 흐름 (요약)', {
    x: 0.4,
    y: 0.35,
    w: 12.5,
    h: 0.5,
    fontSize: 24,
    bold: true,
    color: '1E2A38',
  });
  s.addText(
    '로그인 → 대시보드\n' +
      '사이드바: 주문 관리 · 회원 관리 · 메뉴 관리\n' +
      '로그아웃 → 키오스크 메인(/)',
    { x: 0.5, y: 1, w: 12, h: 3, fontSize: 18, color: '333333', valign: 'top' }
  );
}

const adminSlides = [
  { png: 'admin-login.png', title: '관리자 로그인', sub: 'login.html' },
  { png: 'admin-dashboard.png', title: '대시보드', sub: 'dashboard.html' },
  { png: 'admin-orders.png', title: '주문 관리', sub: 'orders.html' },
  { png: 'admin-members.png', title: '회원 관리', sub: 'members.html' },
  { png: 'admin-menus.png', title: '메뉴 관리', sub: 'menus.html' },
];

for (const a of adminSlides) {
  const s = pptx.addSlide();
  s.addText(a.title, {
    x: 0.4,
    y: 0.25,
    w: 12.5,
    h: 0.45,
    fontSize: 22,
    bold: true,
    color: '1E2A38',
  });
  s.addText(a.sub, {
    x: 0.4,
    y: 0.68,
    w: 12.5,
    h: 0.35,
    fontSize: 12,
    color: '666666',
  });
  s.addImage({
    path: img(a.png),
    x: 0.5,
    y: 1.05,
    w: 12.33,
    h: 5.5,
    rounding: true,
  });
}

await pptx.writeFile({ fileName: outFile });
console.log('Wrote:', outFile);
