/**
 * 브런치 카페 키오스크 - 관리자 페이지 로직
 *
 * [백엔드 연동 포인트]
 * - updateOrderStatus() : PATCH /admin/orders/{id}/status
 * - toggleMenuAvailable() : PATCH /admin/menus/{id}/available
 * - deleteMenu()          : DELETE /admin/menus/{id}
 */

'use strict';

/* ─── 주문 상태 변경 ──────────────────────────────── */
function updateOrderStatus(orderId, selectEl) {
  const newStatus = selectEl.value;
  const statusLabels = {
    RECEIVED:  '접수',
    PREPARING: '준비 중',
    COMPLETED: '완료',
    CANCELLED: '취소'
  };

  if (!confirm(`주문 #${orderId} 상태를 "${statusLabels[newStatus]}"(으)로 변경할까요?`)) {
    // 취소 시 이전 값 복원 (백엔드에서 받아오는 방식으로 대체 가능)
    return;
  }

  /**
   * [백엔드 연동]
   * fetch(`/admin/orders/${orderId}/status`, {
   *   method: 'PATCH',
   *   headers: { 'Content-Type': 'application/json' },
   *   body: JSON.stringify({ status: newStatus })
   * }).then(r => { if (r.ok) showAdminToast('상태가 변경되었습니다.'); });
   */

  // 목업: 배지 색상 즉시 업데이트
  const row = selectEl.closest('tr');
  if (row) {
    const badgeEl = row.querySelector('.status-badge');
    if (badgeEl) {
      badgeEl.className = 'badge ' + statusClassMap(newStatus);
      badgeEl.textContent = statusLabels[newStatus];
    }
  }
  showAdminToast('주문 상태가 변경되었습니다.');
}

function statusClassMap(status) {
  const map = {
    RECEIVED:  'badge-received',
    PREPARING: 'badge-preparing',
    COMPLETED: 'badge-completed',
    CANCELLED: 'badge-cancelled'
  };
  return map[status] || 'badge-received';
}

/* ─── 메뉴 판매 여부 토글 ─────────────────────────── */
function toggleMenuAvailable(menuId, btn) {
  const isAvailable = btn.dataset.available === 'true';
  const newState    = !isAvailable;

  /**
   * [백엔드 연동]
   * fetch(`/admin/menus/${menuId}/available`, {
   *   method: 'PATCH',
   *   headers: { 'Content-Type': 'application/json' },
   *   body: JSON.stringify({ available: newState })
   * }).then(r => { if (r.ok) { ... } });
   */

  btn.dataset.available = String(newState);
  btn.textContent       = newState ? '판매중' : '품절';
  btn.className         = newState ? 'badge badge-available' : 'badge badge-soldout';

  const row   = btn.closest('tr');
  const nameEl = row?.querySelector('.menu-name-cell');
  if (nameEl) nameEl.style.opacity = newState ? '1' : '0.5';

  showAdminToast(newState ? '판매 재개되었습니다.' : '품절 처리되었습니다.');
}

/* ─── 메뉴 삭제 ───────────────────────────────────── */
function deleteMenu(menuId, menuName) {
  if (!confirm(`"${menuName}" 메뉴를 삭제할까요?\n삭제된 메뉴는 복구할 수 없습니다.`)) return;

  /**
   * [백엔드 연동]
   * fetch(`/admin/menus/${menuId}`, { method: 'DELETE' })
   * .then(r => { if (r.ok) document.getElementById(`menu-row-${menuId}`)?.remove(); });
   */

  document.getElementById(`menu-row-${menuId}`)?.remove();
  showAdminToast(`"${menuName}" 메뉴가 삭제되었습니다.`);
}

/* ─── 메뉴 등록/수정 모달 ────────────────────────── */
function openMenuModal(menuId) {
  const modal   = document.getElementById('menu-modal');
  const title   = document.getElementById('menu-modal-title');
  const form    = document.getElementById('menu-form');
  if (!modal) return;

  if (menuId) {
    title.textContent = '메뉴 수정';
    // [백엔드 연동] GET /admin/menus/{menuId} → 폼에 값 채우기
    loadMenuForEdit(menuId, form);
  } else {
    title.textContent = '메뉴 추가';
    form?.reset();
    const previewEl = document.getElementById('menu-img-preview');
    if (previewEl) previewEl.innerHTML = '🍽️';
  }

  modal.classList.add('open');
}

function closeMenuModal() {
  document.getElementById('menu-modal')?.classList.remove('open');
}

function loadMenuForEdit(menuId, form) {
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
}

function submitMenuForm(event) {
  event.preventDefault();
  const form = event.target;
  const data = Object.fromEntries(new FormData(form));

  /**
   * [백엔드 연동]
   * const isEdit = !!data.id;
   * fetch(isEdit ? `/admin/menus/${data.id}` : '/admin/menus', {
   *   method: isEdit ? 'PUT' : 'POST',
   *   headers: { 'Content-Type': 'application/json' },
   *   body: JSON.stringify(data)
   * }).then(r => { if (r.ok) { closeMenuModal(); location.reload(); } });
   */

  showAdminToast('메뉴가 저장되었습니다. (백엔드 연동 후 동작)');
  closeMenuModal();
}

/* ─── 주문 상세 모달 ──────────────────────────────── */
function openOrderDetail(orderId) {
  const modal = document.getElementById('order-detail-modal');
  if (!modal) return;

  /**
   * [백엔드 연동]
   * fetch(`/admin/orders/${orderId}`).then(r => r.json()).then(order => {
   *   renderOrderDetail(order);
   * });
   */

  modal.classList.add('open');
}

function closeOrderDetail() {
  document.getElementById('order-detail-modal')?.classList.remove('open');
}

/* ─── 회원 포인트 수정 ────────────────────────────── */
function editMemberPoints(memberId, currentPoints) {
  const newPoints = prompt(`현재 포인트: ${currentPoints.toLocaleString()} P\n수정할 포인트를 입력하세요:`, currentPoints);
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
  if (el) el.textContent = Number(newPoints).toLocaleString() + ' P';
  showAdminToast('포인트가 수정되었습니다. (백엔드 연동 후 동작)');
}

/* ─── 검색 & 필터 ─────────────────────────────────── */
function filterTable(inputId, tableId) {
  const keyword = document.getElementById(inputId)?.value.toLowerCase().trim() || '';
  const rows    = document.querySelectorAll(`#${tableId} tbody tr`);
  rows.forEach(row => {
    row.style.display = row.textContent.toLowerCase().includes(keyword) ? '' : 'none';
  });
}

/* ─── 사이드바 활성 메뉴 자동 표시 ───────────────── */
function highlightActiveNav() {
  const path = window.location.pathname;
  document.querySelectorAll('.nav-item').forEach(item => {
    const href = item.getAttribute('href') || '';
    item.classList.toggle('active', href !== '#' && path.startsWith(href));
  });
}

/* ─── 대시보드 날짜 표시 ──────────────────────────── */
function updateDashboardDate() {
  const el = document.getElementById('dashboard-date');
  if (!el) return;
  const now = new Date();
  el.textContent = now.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' });
}

/* ─── Toast ────────────────────────────────────────── */
function showAdminToast(msg, type = 'success') {
  let toast = document.getElementById('admin-toast');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'admin-toast';
    toast.style.cssText = `
      position:fixed; bottom:28px; right:28px; z-index:9999;
      background:#1E2A38; color:white; padding:12px 22px;
      border-radius:10px; font-size:14px; font-weight:600;
      box-shadow:0 4px 20px rgba(0,0,0,0.2);
      transition:all 0.3s; transform:translateY(20px); opacity:0;
    `;
    document.body.appendChild(toast);
  }
  toast.textContent = msg;
  toast.style.transform = 'translateY(0)';
  toast.style.opacity   = '1';
  clearTimeout(toast._t);
  toast._t = setTimeout(() => {
    toast.style.transform = 'translateY(20px)';
    toast.style.opacity   = '0';
  }, 2800);
}

/* ─── 모달 ESC 닫기 ───────────────────────────────── */
document.addEventListener('keydown', e => {
  if (e.key === 'Escape') {
    closeMenuModal();
    closeOrderDetail();
  }
});

/* ─── 모달 배경 클릭 닫기 ────────────────────────── */
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-overlay')) {
    closeMenuModal();
    closeOrderDetail();
  }
});

/* ─── 이미지 URL 미리보기 ─────────────────────────── */
function previewMenuImage() {
  const urlInput  = document.getElementById('menu-image-url');
  const previewEl = document.getElementById('menu-img-preview');
  if (!urlInput || !previewEl) return;

  const url = urlInput.value.trim();
  if (url) {
    previewEl.innerHTML = `<img src="${url}" alt="미리보기" onerror="this.parentElement.innerHTML='🍽️'">`;
  } else {
    previewEl.innerHTML = '🍽️';
  }
}

/* ─── 초기화 ──────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  highlightActiveNav();
  updateDashboardDate();

  // 검색 박스 실시간 필터
  const orderSearch  = document.getElementById('order-search');
  const memberSearch = document.getElementById('member-search');
  const menuSearch   = document.getElementById('menu-search');

  orderSearch?.addEventListener('input',  () => filterTable('order-search',  'orders-table'));
  memberSearch?.addEventListener('input', () => filterTable('member-search', 'members-table'));
  menuSearch?.addEventListener('input',   () => filterTable('menu-search',   'menus-table'));
});
