/**
 * 브런치 카페 키오스크 - 장바구니 및 UI 로직
 *
 * [백엔드 연동 포인트]
 * - checkout()       : POST /order/cart  (cartData JSON 전송)
 * - applyCoupon()    : POST /coupon/check (AJAX)
 */

"use strict";

/* ─── Cart State ──────────────────────────────────── */
const Cart = (() => {
  const STORAGE_KEY = "cafe_cart";

  let state = {
    items: [], // [{id, name, price, quantity, emoji}]
    memberId: null,
    memberName: null,
    memberPoints: 0,
    couponCode: null,
    couponDiscount: 0,
    usePoints: 0,
  };

  /* 저장 & 불러오기 */
  function save() {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state.items));
  }

  function load() {
    try {
      const saved = sessionStorage.getItem(STORAGE_KEY);
      if (saved) state.items = JSON.parse(saved);
    } catch (e) {
      state.items = [];
    }
  }

  /* 계산 */
  function getSubtotal() {
    return state.items.reduce((sum, i) => sum + i.price * i.quantity, 0);
  }

  function getTotalCount() {
    return state.items.reduce((sum, i) => sum + i.quantity, 0);
  }

  function getFinalTotal() {
    return Math.max(0, getSubtotal() - state.couponDiscount);
  }

  /* 담기 */
  function add(id, name, price, emoji) {
    id = Number(id);
    price = Number(price);
    const existing = state.items.find((i) => i.id === id);
    if (existing) {
      existing.quantity++;
    } else {
      state.items.push({
        id,
        name,
        price,
        quantity: 1,
        imgUrl: emoji || "/images/menu/default-food.svg",
      });
    }
    save();
    render();
    showToast(`${name} 담겼습니다!`);
  }

  /* 수량 변경 */
  function updateQty(id, delta) {
    id = Number(id);
    const item = state.items.find((i) => i.id === id);
    if (!item) return;
    item.quantity += delta;
    if (item.quantity <= 0) {
      state.items = state.items.filter((i) => i.id !== id);
    }
    save();
    render();
  }

  /* 삭제 */
  function remove(id) {
    state.items = state.items.filter((i) => i.id !== Number(id));
    save();
    render();
  }

  /* 초기화 */
  function clear() {
    state.items = [];
    state.couponDiscount = 0;
    state.couponCode = null;
    save();
    render();
  }

  /* ─── 렌더링 ─────────────────────────────────────── */
  function render() {
    renderCartPanel();
    updateCounts();
  }

  function renderCartPanel() {
    const container = document.getElementById("cart-items");
    const emptyEl = document.getElementById("cart-empty");
    const checkoutBtn = document.getElementById("btn-checkout");
    const clearBtn = document.getElementById("btn-cart-clear");

    if (!container) return;

    if (state.items.length === 0) {
      container.innerHTML = "";
      if (emptyEl) emptyEl.style.display = "flex";
      if (checkoutBtn) checkoutBtn.disabled = true;
      if (clearBtn) clearBtn.style.display = "none";
      setGrandTotal(0);
      return;
    }

    if (emptyEl) emptyEl.style.display = "none";
    if (checkoutBtn) checkoutBtn.disabled = false;
    if (clearBtn) clearBtn.style.display = "block";

    container.innerHTML = state.items
      .map(
        (item) => `
      <div class="cart-item" data-id="${item.id}">
        <img class="cart-item-thumb"
             src="${escHtml(item.imgUrl || "/images/menu/empty.png")}"
             alt="${escHtml(item.name)}"
             onerror="this.src='/images/menu/empty.png'">
        <div class="cart-item-info">
          <div class="cart-item-name">${escHtml(item.name)}</div>
          <div class="cart-item-price">${fmtPrice(item.price * item.quantity)}</div>
        </div>
        <div class="qty-control">
          <button class="qty-btn" onclick="Cart.updateQty(${item.id}, -1)" aria-label="수량 감소">−</button>
          <span class="qty-value">${item.quantity}</span>
          <button class="qty-btn" onclick="Cart.updateQty(${item.id}, 1)" aria-label="수량 증가">+</button>
        </div>
      </div>
    `,
      )
      .join("");

    setGrandTotal(getFinalTotal());
  }

  function setGrandTotal(amount) {
    const el = document.getElementById("cart-total");
    if (el) el.textContent = fmtPrice(amount);
  }

  function updateCounts() {
    const badge = document.getElementById("cart-count");
    if (badge) badge.textContent = getTotalCount();
  }

  /* ─── 주문하기 ──────────────────────────────────── */
  function checkout() {
    if (state.items.length === 0) {
      showToast("장바구니가 비어있습니다.");
      return;
    }
    const form = document.getElementById("checkout-form");
    const dataInput = document.getElementById("cart-data");
    if (!form || !dataInput) return;

    dataInput.value = JSON.stringify({
      items: state.items,
      couponCode: state.couponCode,
      couponDiscount: state.couponDiscount,
      usePoints: state.usePoints,
      memberId: state.memberId,
    });

    form.submit();
  }

  /* ─── 공개 API ───────────────────────────────────── */
  return {
    init: () => {
      load();
      render();
    },
    add,
    updateQty,
    remove,
    clear,
    checkout,
    getState: () => state,
  };
})();

/* ─── 탭 전환 ─────────────────────────────────────── */
function switchTab(tab) {
  document.querySelectorAll(".tab-btn").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.tab === tab);
  });
  document.querySelectorAll(".menu-grid-wrapper").forEach((el) => {
    el.style.display = el.dataset.tab === tab ? "block" : "none";
  });
}

/* ─── 회원 조회 (장바구니 확인 페이지) ────────────── */
// [API] POST /api/member/lookup
// 요청: MemberLookupRequest { phone }
// 응답: MemberLookupResponse { memberName, points, unusedCouponCount, coupons[] }
async function lookupMemberByPhone() {
  const phone =
    document.getElementById("lookup-phone-input")?.value.replace(/\D/g, "") ||
    "";
  const errorBox = document.getElementById("lookup-error-box");
  const resultPanel = document.getElementById("lookup-result-panel");

  if (phone.length < 10) {
    showToast("전화번호를 올바르게 입력해주세요. (10~11자리)");
    return;
  }

  if (errorBox) errorBox.style.display = "none";
  if (resultPanel) resultPanel.style.display = "none";

  try {
    const res = await fetch("/api/member/lookup", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ phone }),
    });

    if (res.status === 404) {
      if (errorBox) {
        errorBox.textContent = "가입되지 않은 전화번호입니다.";
        errorBox.style.display = "block";
      }
      return;
    }

    if (!res.ok) {
      if (errorBox) {
        errorBox.textContent =
          "조회 중 오류가 발생했습니다. 다시 시도해주세요.";
        errorBox.style.display = "block";
      }
      return;
    }

    // MemberLookupResponse: { memberName, points, unusedCouponCount, coupons[] }
    const data = await res.json();

    Cart.getState().memberId = data.memberId;
    Cart.getState().memberName = data.memberName;

    console.log(data.memberId);
    console.log(data);

    document.getElementById("hidden-member-name").value = data.memberName || "";
    document.getElementById("hidden-member-id").value = data.memberId || 0;

    document.getElementById("lookup-member-name").textContent =
      data.memberName || "";
    document.getElementById("lookup-coupon-count").textContent =
      (data.unusedCouponCount || 0) + "개";

    // 미사용 쿠폰 목록 렌더링
    const couponListEl = document.getElementById("lookup-coupon-list");
    if (couponListEl) {
      const unused = (data.coupons || []).filter((c) => !c.used);
      if (unused.length === 0) {
        couponListEl.innerHTML =
          '<div class="lookup-empty-coupons">사용 가능한 쿠폰이 없습니다.</div>';
      } else {
        couponListEl.innerHTML = unused
          .map((c) => {
            const discount =
              c.discountType === "FIXED"
                ? c.discountValue.toLocaleString("ko-KR") + "원 할인"
                : c.discountValue + "% 할인";
            return `<div class="lookup-coupon-check-row">
            <div>
              <div class="lookup-coupon-title">${c.name || "쿠폰"}</div>
              <div class="lookup-coupon-benefit">${discount}</div>
              <div class="lookup-coupon-code">${c.code}</div>
            </div>
            <button class="cart-lookup-coupon-pick"
              onclick="applyLookupCoupon('${c.code}', '${c.discountType}', ${c.discountValue}, '${c.name || "쿠폰"}')">
              쿠폰 적용
            </button>
          </div>`;
          })
          .join("");
      }
    }

    if (resultPanel) resultPanel.style.display = "block";
  } catch (e) {
    if (errorBox) {
      errorBox.textContent = "네트워크 오류가 발생했습니다.";
      errorBox.style.display = "block";
    }
  }
}

// 쿠폰 목록에서 "쿠폰 적용" 클릭 시 호출
// 결제 금액에서 차감하고 hidden-coupon-code에 코드 저장
function applyLookupCoupon(code, discountType, discountValue, name) {
  const subtotal = Number(
    document.getElementById("subtotal-value")?.dataset.amount || 0,
  );
  let discount =
    discountType === "FIXED"
      ? discountValue
      : Math.round((subtotal * discountValue) / 100);
  discount = Math.min(discount, subtotal);

  updateDiscount(discount, `${name} (${code}) 적용`);

  // 결제 폼 hidden input에 쿠폰 코드 세팅 → POST /order/payment 전송
  const hiddenCode = document.getElementById("hidden-coupon-code");
  if (hiddenCode) hiddenCode.value = code;

  Cart.getState().couponCode = code;
  Cart.getState().couponDiscount = discount;
}

function updateDiscount(amount, label) {
  const resultEl = document.getElementById("coupon-result");
  const discountEl = document.getElementById("coupon-discount-value");

  if (resultEl) {
    resultEl.textContent = label;
    resultEl.className = "discount-result visible";
    resultEl.style.background = "";
    resultEl.style.borderColor = "";
    resultEl.style.color = "";
  }
  if (discountEl) {
    discountEl.textContent = amount > 0 ? `-${fmtPrice(amount)}` : "-";
    discountEl.dataset.amount = amount;
  }
  recalcTotal();
}

function recalcTotal() {
  const subtotal = Number(
    document.getElementById("subtotal-value")?.dataset.amount || 0,
  );
  const couponAmt = Number(
    document.getElementById("coupon-discount-value")?.dataset.amount || 0,
  );

  const total = Math.max(0, subtotal - couponAmt);
  const totalEl = document.getElementById("final-total-value");
  if (totalEl) totalEl.textContent = fmtPrice(total);
}

/* ─── 결제 수단 선택 ──────────────────────────────── */
function selectPayMethod(method) {
  document.querySelectorAll(".pay-method-card").forEach((card) => {
    card.classList.toggle("selected", card.dataset.method === method);
  });
  const input = document.getElementById("payment-method-input");
  if (input) input.value = method;

  const confirmBtn = document.getElementById("btn-confirm-pay");
  if (confirmBtn) confirmBtn.disabled = false;
}

/* ─── 결제 확인 ───────────────────────────────────── */
function confirmPayment() {
  const method = document.getElementById("payment-method-input")?.value;
  if (!method) {
    showToast("결제 수단을 선택해주세요.");
    return;
  }
  const btn = document.getElementById("btn-confirm-pay");
  if (btn) {
    btn.disabled = true;
    btn.textContent = "결제 처리 중...";
  }

  // 모의 결제 처리 (2초 딜레이 후 완료 페이지로 이동)
  setTimeout(() => {
    const form = document.getElementById("payment-form");
    if (form) {
      form.submit();
    } else {
      window.location.href = "/order/complete";
    }
  }, 1800);
}

/* ─── 전화번호 입력 (적립 페이지) ────────────────── */
const PhoneInput = (() => {
  let digits = "";
  const MAX = 11;

  function push(d) {
    if (digits.length >= MAX) return;
    digits += d;
    render();
    if (digits.length === MAX) lookup();
  }

  function del() {
    digits = digits.slice(0, -1);
    render();
    hideMemberInfo();
  }

  function clearAll() {
    digits = "";
    render();
    hideMemberInfo();
  }

  function render() {
    const el = document.getElementById("phone-display");
    if (!el) return;
    if (digits.length === 0) {
      el.textContent = "전화번호를 입력하세요";
      el.classList.add("placeholder");
      return;
    }
    el.classList.remove("placeholder");
    el.textContent = formatPhone(digits);
  }

  function formatPhone(d) {
    if (d.length <= 3) return d;
    if (d.length <= 7) return `${d.slice(0, 3)}-${d.slice(3)}`;
    return `${d.slice(0, 3)}-${d.slice(3, 7)}-${d.slice(7)}`;
  }

  /** 11자리 입력 완료 시: 서버 조회 없이 hidden에만 반영 (적립은 POST /member/stamp 에서 처리) */
  function lookup() {
    const input = document.getElementById("phone-value-input");
    if (input) input.value = digits;
    const card = document.getElementById("member-found-card");
    if (!card) return;
    card.style.display = "block";
    document.getElementById("member-found-name").textContent =
      "전화번호 입력 완료";
    document.getElementById("member-found-points").textContent =
      "적립하기를 눌러주세요.";
  }

  function hideMemberInfo() {
    const card = document.getElementById("member-found-card");
    if (card) card.style.display = "none";
  }

  return { push, del, clearAll };
})();

/* ─── 시계 ────────────────────────────────────────── */
function startClock() {
  const el = document.getElementById("header-clock");
  if (!el) return;
  function tick() {
    const now = new Date();
    el.textContent = now.toLocaleTimeString("ko-KR", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });
  }
  tick();
  setInterval(tick, 1000);
}

/* ─── 유틸리티 ────────────────────────────────────── */
function fmtPrice(n) {
  return Number(n).toLocaleString("ko-KR") + "원";
}

function escHtml(str) {
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function showToast(msg) {
  let toast = document.getElementById("toast");
  if (!toast) {
    toast = document.createElement("div");
    toast.id = "toast";
    toast.className = "toast-notification";
    document.body.appendChild(toast);
  }
  toast.textContent = msg;
  toast.classList.add("show");
  clearTimeout(toast._timer);
  toast._timer = setTimeout(() => toast.classList.remove("show"), 2400);
}

/* ─── 초기화 ──────────────────────────────────────── */
document.addEventListener("DOMContentLoaded", () => {
  Cart.init();
  startClock();

  // 첫 번째 탭 활성화
  const firstTab = document.querySelector(".tab-btn");
  if (firstTab && firstTab.dataset.tab) {
    switchTab(firstTab.dataset.tab);
  }

  // 결제 수단 카드 클릭 이벤트 위임
  document.querySelectorAll(".pay-method-card").forEach((card) => {
    card.addEventListener("click", () => selectPayMethod(card.dataset.method));
  });
});
