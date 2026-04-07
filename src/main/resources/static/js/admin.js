/**
 * 브런치 카페 키오스크 - 관리자 페이지 로직
 *
 * [백엔드 연동 포인트]
 * - updateOrderStatus() : PATCH /admin/orders/{id}/status
 * - toggleMenuAvailable() : PATCH /admin/menus/{id}/available
 * - deleteMenu()          : DELETE /admin/menus/{id}
 */

"use strict";

function adminCsrfHeaders() {
  const token = document
    .querySelector('meta[name="_csrf"]')
    ?.getAttribute("content");
  const headerName = document
    .querySelector('meta[name="_csrf_header"]')
    ?.getAttribute("content");
  const headers = { "Content-Type": "application/json" };
  if (token && headerName) {
    headers[headerName] = token;
  }
  return headers;
}
/* ─── 주문 상태 변경 ──────────────────────────────── */
async function updateOrderStatus(orderId, selectEl) {
  if (!selectEl || orderId == null) {
    return;
  }

  const previous =
    selectEl.getAttribute("data-original-status") || selectEl.value;
  const newStatus = selectEl.value;
  const statusLabels = {
    RECEIVED: "접수",
    PREPARING: "준비 중",
    COMPLETED: "완료",
    CANCELLED: "취소",
  };

  if (
    !confirm(
      `주문 #${orderId} 상태를 "${statusLabels[newStatus]}"(으)로 변경할까요?`,
    )
  ) {
    selectEl.value = previous;
    showAdminToast("변경을 취소했습니다.");
    return;
  }

  try {
    const res = await fetch(`/admin/orders/${orderId}/status`, {
      method: "PATCH",
      credentials: "same-origin",
      headers: adminCsrfHeaders(),
      body: JSON.stringify({ status: newStatus }),
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || res.statusText);
    }

    try {
      await res.json();
    } catch (_) {
      /* 본문 없음·비 JSON */
    }

    selectEl.setAttribute("data-original-status", newStatus);
    const row = selectEl.closest("tr");
    const badgeEl = row?.querySelector(".status-badge");
    if (badgeEl) {
      badgeEl.className = "badge " + statusClassMap(newStatus);
      badgeEl.textContent = statusLabels[newStatus];
    }
    showAdminToast("주문 상태가 변경되었습니다.");
  } catch (e) {
    selectEl.value = previous;
    showAdminToast("상태 변경에 실패했습니다.", "error");
  }
}

window.updateOrderStatus = updateOrderStatus;

function statusClassMap(status) {
  const map = {
    RECEIVED: "badge-received",
    PREPARING: "badge-preparing",
    COMPLETED: "badge-completed",
    CANCELLED: "badge-cancelled",
  };
  return map[status] || "badge-received";
}

/* ─── 메뉴 판매 여부 토글 ─────────────────────────── */
async function toggleMenuAvailable(menuId, btn) {
  const isAvailable = btn.dataset.available === "true";
  const newState = !isAvailable;

  /**
   * [백엔드 연동]
   * fetch(`/admin/menus/${menuId}/available`, {
   *   method: 'PATCH',
   *   headers: { 'Content-Type': 'application/json' },
   *   body: JSON.stringify({ available: newState })
   * }).then(r => { if (r.ok) { ... } });
   */

  try {
    const res = await fetch(`/admin/menus/${menuId}/available`, {
      method: "PATCH",
      headers: adminCsrfHeaders(),
      body: JSON.stringify({ available: newState }),
    });
    if (!res.ok) throw new Error("상태 변경 실패");

    btn.dataset.available = String(newState);
    btn.textContent = newState ? "판매중" : "품절";
    btn.className = newState ? "badge badge-available" : "badge badge-soldout";
    btn.style = "border:none; cursor:pointer; padding:4px 12px;";

    const nameEl = btn.closest("tr")?.querySelector(".menu-name-cell");
    if (nameEl) nameEl.style.opacity = newState ? "1" : "0.5";

    showAdminToast(newState ? "판매 재개되었습니다." : "품절 처리되었습니다.");
  } catch (e) {
    showAdminToast("상태 변경에 실패했습니다.", "error");
  }
}

/* ─── 메뉴 삭제 ───────────────────────────────────── */
async function deleteMenu(menuId, menuName) {
  if (
    !confirm(
      `"${menuName}" 메뉴를 삭제할까요?\n삭제된 메뉴는 복구할 수 없습니다.`,
    )
  )
    return;

  /**
   * [백엔드 연동]
   * fetch(`/admin/menus/${menuId}`, { method: 'DELETE' })
   * .then(r => { if (r.ok) document.getElementById(`menu-row-${menuId}`)?.remove(); });
   */
  try {
    const res = await fetch(`/admin/menus/${menuId}`, {
      method: "DELETE",
      headers: adminCsrfHeaders(),
    });
    if (!res.ok) throw new Error("삭제 실패");

    document.getElementById(`menu-row-${menuId}`)?.remove();
    showAdminToast(`"${menuName}" 메뉴가 삭제되었습니다.`);
  } catch (e) {
    showAdminToast("삭제에 실패했습니다.", "error");
  }
}

/* ─── 메뉴 등록/수정 모달 ────────────────────────── */
function openMenuModal(menuId) {
  const modal = document.getElementById("menu-modal");
  const title = document.getElementById("menu-modal-title");
  const form = document.getElementById("menu-form");
  if (!modal) return;

  if (menuId) {
    title.textContent = "메뉴 수정";
    // [백엔드 연동] GET /admin/menus/{menuId} → 폼에 값 채우기
    loadMenuForEdit(menuId, form);
  } else {
    title.textContent = "메뉴 추가";
    form?.reset();
    const previewEl = document.getElementById("menu-img-preview");
    if (previewEl)
      previewEl.innerHTML =
        "<img src='/images/menu/empty.png' alt='미리보기' />";
  }

  modal.classList.add("open");
}

function closeMenuModal() {
  document.getElementById("menu-modal")?.classList.remove("open");
}

async function loadMenuForEdit(menuId, form) {
  /**
   * [백엔드 연동]
   * fetch(`/admin/menus/${menuId}`).then(r => r.json()).then(menu => {
   *   form.querySelector('[name=name]').value      = menu.name;
   *   form.querySelector('[name=price]').value     = menu.price;
   *   form.querySelector('[name=category]').value  = menu.category;
   *   form.querySelector('[name=description]').value = menu.description;
   *   form.querySelector('[name=available]').checked = menu.available;
   *   form.querySelector('[name=id]').value        = menu.id;
   * });
   */
  try {
    const res = await fetch(`/admin/menus/${menuId}`);
    if (!res.ok) throw new Error("메뉴 조회 실패");
    const menu = await res.json();

    form.querySelector("[name=id]").value = menu.id;
    form.querySelector("[name=name]").value = menu.name;
    form.querySelector("[name=price]").value = menu.price;
    form.querySelector("[name=category]").value = menu.category;
    form.querySelector("[name=description]").value = menu.description || "";
    form.querySelector("[name=imageUrl]").value = menu.imageUrl || "";

    const previewEl = document.getElementById("menu-img-preview");
    if (previewEl && menu.imageUrl) {
      previewEl.innerHTML = `<img src="${menu.imageUrl}" alt="미리보기" style="width:60px; height:60px; object-fit:cover;">`;
    } else {
      previewEl.innerHTML = `<img src="/images/menu/empty.png" alt="미리보기">`;
    }
  } catch (e) {
    showAdminToast("메뉴 정보를 불러오지 못했습니다.", "error");
  }
}

async function submitMenuForm(event) {
  event.preventDefault();
  const form = event.target;

  const id = form.querySelector("[name=id]").value;
  const isEdit = !!id;

  /**
   * [백엔드 연동]
   * const isEdit = !!data.id;
   * fetch(isEdit ? `/admin/menus/${data.id}` : '/admin/menus', {
   *   method: isEdit ? 'PUT' : 'POST',
   *   headers: { 'Content-Type': 'application/json' },
   *   body: JSON.stringify(data)
   * }).then(r => { if (r.ok) { closeMenuModal(); location.reload(); } });
   */

  const body = {
    name: form.querySelector("[name=name]").value,
    price: Number(form.querySelector("[name=price]").value),
    category: form.querySelector("[name=category]").value,
    description: form.querySelector("[name=description]").value,
    imageUrl: form.querySelector("[name=imageUrl]").value || "",
    available: form.querySelector("[name=available]").checked,
  };

  try {
    const res = await fetch(isEdit ? `/admin/menus/${id}` : "/admin/menus", {
      method: isEdit ? "PUT" : "POST",
      headers: adminCsrfHeaders(),
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error("저장 실패");

    showAdminToast("메뉴가 저장되었습니다.");
    closeMenuModal();
    location.reload();
  } catch (e) {
    showAdminToast("저장에 실패했습니다.", "error");
  }
}

/* ─── 주문 상세 모달 ──────────────────────────────── */
function openOrderDetail(orderId) {
  const modal = document.getElementById("order-detail-modal");
  if (!modal) return;

  /**
   * [백엔드 연동]
   * fetch(`/admin/orders/${orderId}`).then(r => r.json()).then(order => {
   *   renderOrderDetail(order);
   * });
   */

  modal.classList.add("open");
}

function closeOrderDetail() {
  document.getElementById("order-detail-modal")?.classList.remove("open");
}

/* ─── 회원 포인트 수정 ────────────────────────────── */
function editMemberPoints(memberId, currentPoints) {
  const newPoints = prompt(
    `현재 포인트: ${currentPoints.toLocaleString()} P\n수정할 포인트를 입력하세요:`,
    currentPoints,
  );
  if (newPoints === null || isNaN(newPoints)) return;

  /**
   * [백엔드 연동]
   * fetch(`/admin/members/${memberId}/points`, {
   *   method: 'PATCH',
   *   headers: { 'Content-Type': 'application/json' },
   *   body: JSON.stringify({ points: Number(newPoints) })
   * }).then(r => { if (r.ok) location.reload(); });
   */

  const el = document.getElementById(`member-points-${memberId}`);
  if (el) el.textContent = Number(newPoints).toLocaleString() + " P";
  showAdminToast("포인트가 수정되었습니다. (백엔드 연동 후 동작)");
}

/* ─── 회원 추가/삭제 ───────────────────────────────── */
function openAddMemberModal() {
  const modal = document.getElementById("add-member-modal");
  if (!modal) return;

  const msgEl = document.getElementById("add-member-msg");
  if (msgEl) {
    msgEl.textContent = "";
    msgEl.style.color = "";
  }

  const phoneInput = document.getElementById("add-member-phone");
  if (phoneInput) {
    phoneInput.value = "";
    phoneInput.focus();
  }

  const nameInput = document.getElementById("add-member-name");
  if (nameInput) nameInput.value = "";

  modal.style.display = "flex";
}

function closeAddMemberModal() {
  const modal = document.getElementById("add-member-modal");
  if (!modal) return;
  modal.style.display = "none";
}

async function addMember() {
  const phoneRaw = document.getElementById("add-member-phone")?.value || "";
  const nameRaw = document.getElementById("add-member-name")?.value || "";

  const msgEl = document.getElementById("add-member-msg");
  if (msgEl) {
    msgEl.textContent = "";
    msgEl.style.color = "#dc2626";
    msgEl.style.background = "#fee2e2";
    msgEl.style.border = "1px solid #dc2626";
    msgEl.style.marginTop = "10px";
    msgEl.style.fontSize = "13px";
    msgEl.style.borderRadius = "8px";
    msgEl.style.padding = "10px 14px";
  }

  const phoneDigits = phoneRaw.replace(/\D/g, "");
  if (phoneDigits.length < 10 || phoneDigits.length > 11) {
    if (msgEl) msgEl.textContent = "전화번호는 10~11자리 숫자로 입력해주세요.";
    return;
  }

  try {
    const res = await fetch("/admin/api/members", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ phone: phoneRaw, name: nameRaw }),
    });

    const data = await res.json().catch(() => ({}));
    if (res.ok) {
      showAdminToast("회원이 추가되었습니다.");
      closeAddMemberModal();
      location.reload();
      return;
    }

    if (msgEl)
      msgEl.textContent = data.error || "회원 추가 중 오류가 발생했습니다.";
  } catch (e) {
    if (msgEl) msgEl.textContent = "네트워크 오류가 발생했습니다.";
  }
}

async function deleteMember(btnEl) {
  const memberId = btnEl?.dataset?.memberId;
  const memberName = btnEl?.dataset?.memberName || "해당 회원";
  if (!memberId) return;

  if (
    !confirm(
      `${memberName} 회원을 삭제할까요?\n(쿠폰/주문이 있으면 삭제가 거부됩니다.)`,
    )
  )
    return;

  try {
    btnEl.disabled = true;
    btnEl.textContent = "삭제 중...";

    const res = await fetch(`/admin/api/members/${memberId}`, {
      method: "DELETE",
    });
    const data = await res.json().catch(() => ({}));

    if (res.ok) {
      showAdminToast("회원이 삭제되었습니다.");
      location.reload();
      return;
    }

    const message = data.error || "삭제 중 오류가 발생했습니다.";
    showAdminToast(message, "error");
  } catch (e) {
    showAdminToast("네트워크 오류가 발생했습니다.", "error");
  } finally {
    btnEl.disabled = false;
    btnEl.textContent = "삭제";
  }
}

/* ─── 검색 & 필터 ─────────────────────────────────── */
function filterTable(inputId, tableId) {
  const keyword =
    document.getElementById(inputId)?.value.toLowerCase().trim() || "";
  const rows = document.querySelectorAll(`#${tableId} tbody tr`);
  rows.forEach((row) => {
    row.style.display = row.textContent.toLowerCase().includes(keyword)
      ? ""
      : "none";
  });
}

/* ─── 사이드바 활성 메뉴 자동 표시 ───────────────── */
function highlightActiveNav() {
  const path = window.location.pathname;
  document.querySelectorAll(".nav-item").forEach((item) => {
    const href = item.getAttribute("href") || "";
    item.classList.toggle("active", href !== "#" && path.startsWith(href));
  });
}

/* ─── 대시보드 날짜 표시 ──────────────────────────── */
function updateDashboardDate() {
  const el = document.getElementById("dashboard-date");
  if (!el) return;
  const now = new Date();
  el.textContent = now.toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "long",
  });
}

/* ─── Toast ────────────────────────────────────────── */
function showAdminToast(msg, type = "success") {
  const bg = type === "error" ? "#b71c1c" : "#1E2A38";
  let toast = document.getElementById("admin-toast");
  if (!toast) {
    toast = document.createElement("div");
    toast.id = "admin-toast";
    toast.setAttribute("role", "status");
    toast.style.cssText = `
      position:fixed; bottom:28px; right:28px; left:auto; z-index:2147483000;
      max-width:min(90vw, 360px); word-break:break-word;
      background:${bg}; color:white; padding:12px 22px;
      border-radius:10px; font-size:14px; font-weight:600;
      box-shadow:0 4px 20px rgba(0,0,0,0.2);
      transition:opacity 0.25s, transform 0.25s; transform:translateY(20px); opacity:0;
      pointer-events:none; visibility:visible;
    `;
    document.body.appendChild(toast);
  }
  toast.style.background = bg;
  toast.textContent = msg;
  toast.style.visibility = "visible";
  toast.style.transform = "translateY(0)";
  toast.style.opacity = "1";
  clearTimeout(toast._t);
  toast._t = setTimeout(() => {
    toast.style.transform = "translateY(20px)";
    toast.style.opacity = "0";
  }, 2800);
}

/* ─── 모달 ESC 닫기 ───────────────────────────────── */
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") {
    closeMenuModal();
    closeOrderDetail();
    closeAddMemberModal();
  }
});

/* ─── 모달 배경 클릭 닫기 ────────────────────────── */
document.addEventListener("click", (e) => {
  if (e.target.classList.contains("modal-overlay")) {
    closeMenuModal();
    closeOrderDetail();
  }
});

/* ─── 이미지 URL 미리보기 ─────────────────────────── */
function previewMenuImage() {
  const urlInput = document.getElementById("menu-image-url");
  const previewEl = document.getElementById("menu-img-preview");
  if (!urlInput || !previewEl) return;

  const url = urlInput.value.trim();
  if (url) {
    previewEl.innerHTML = `<img src="${url}" alt="미리보기" onerror="this.parentElement.innerHTML='🍽️'">`;
  } else {
    previewEl.innerHTML = "🍽️";
  }
}

/* ─── 초기화 ──────────────────────────────────────── */
document.addEventListener("DOMContentLoaded", () => {
  highlightActiveNav();
  updateDashboardDate();

  // 검색 박스 실시간 필터
  const orderSearch = document.getElementById("order-search");
  const memberSearch = document.getElementById("member-search");
  const menuSearch = document.getElementById("menu-search");

  orderSearch?.addEventListener("input", () =>
    filterTable("order-search", "orders-table"),
  );
  memberSearch?.addEventListener("input", () =>
    filterTable("member-search", "members-table"),
  );
  menuSearch?.addEventListener("input", () =>
    filterTable("menu-search", "menus-table"),
  );

  const ordersTable = document.getElementById("orders-table");
  ordersTable?.addEventListener("change", (e) => {
    const sel = e.target;
    if (!sel || !sel.classList || !sel.classList.contains("status-select")) {
      return;
    }
    const row = sel.closest("tr");
    const rawId = row?.dataset?.orderId;
    if (rawId == null || rawId === "") {
      return;
    }
    const orderId = Number(rawId);
    if (Number.isNaN(orderId)) {
      return;
    }
    void updateOrderStatus(orderId, sel);
  });
});
