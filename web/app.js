/* =========================================================
   Campus Academic Systems - Frontend Dashboard Logic
   ========================================================= */

const SLOTS = [
  "06:00-07:00", "07:00-08:00", "08:00-09:00",
  "09:00-10:00", "10:00-11:00", "16:00-17:00",
  "17:00-18:00", "18:00-19:00", "19:00-20:00"
];

let state = {
  users: [],
  courts: [],
  hostelStudents: [],
  selectedUserId: null,
  selectedStudentRoll: null,
  activeFineTargetUser: null,
  activeLeaveTargetStudent: null
};

// ==========================================
// Initialization
// ==========================================
document.addEventListener("DOMContentLoaded", () => {
  setupTabs();
  setupEventListeners();
  loadData();
});

function setupTabs() {
  const tabs = document.querySelectorAll(".nav-tab");
  tabs.forEach(tab => {
    tab.addEventListener("click", () => {
      tabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");

      const targetTab = tab.dataset.tab;
      document.querySelectorAll(".tab-pane").forEach(pane => {
        pane.classList.toggle("active", pane.id === `tab-${targetTab}`);
      });
    });
  });
}

function setupEventListeners() {
  document.getElementById("userSelector").addEventListener("change", (e) => {
    state.selectedUserId = e.target.value;
    updateUserBadge();
  });

  document.getElementById("saveBtn").addEventListener("click", saveSystemState);

  // Hostel Register Modal
  document.getElementById("openRegisterHostelBtn").addEventListener("click", () => {
    openModal("registerModal");
  });
  document.getElementById("submitRegisterBtn").addEventListener("click", handleRegisterHostelStudent);

  // Fine Modal
  document.getElementById("submitFineBtn").addEventListener("click", handleSubmitFine);

  // Leave Modal
  document.getElementById("submitLeaveBtn").addEventListener("click", handleSubmitLeave);
}

// ==========================================
// Data Fetching & Sync
// ==========================================
async function loadData() {
  try {
    const res = await fetch("/api/data");
    if (!res.ok) throw new Error("Failed to load campus data");
    const data = await res.json();

    state.users = data.users || [];
    state.courts = data.courts || [];
    state.hostelStudents = data.hostelStudents || [];

    if (!state.selectedUserId && state.users.length > 0) {
      state.selectedUserId = state.users[0].userId;
    }

    renderUserSelector();
    renderStats();
    renderCourts();
    renderHostelStudents();
    renderUsersGrid();

    // If a student was selected for billing, refresh bill
    if (state.selectedStudentRoll) {
      fetchAndRenderBill(state.selectedStudentRoll);
    }
  } catch (err) {
    showToast("Error loading campus data: " + err.message, "error");
  }
}

// ==========================================
// Rendering: User Selector & KPI
// ==========================================
function renderUserSelector() {
  const select = document.getElementById("userSelector");
  select.innerHTML = "";

  state.users.forEach(u => {
    const opt = document.createElement("option");
    opt.value = u.userId;
    opt.textContent = `${u.name} (${u.role})`;
    if (u.userId === state.selectedUserId) {
      opt.selected = true;
    }
    select.appendChild(opt);
  });

  updateUserBadge();
}

function updateUserBadge() {
  const badge = document.getElementById("userBadge");
  const user = state.users.find(u => u.userId === state.selectedUserId);
  if (!user) return;

  badge.textContent = user.role;
  badge.className = "badge";
  if (user.role === "Student") badge.classList.add("badge-student");
  else if (user.role === "Faculty") badge.classList.add("badge-faculty");
  else if (user.role === "Coach") badge.classList.add("badge-coach");
  else badge.classList.add("badge-neutral");
}

function renderStats() {
  document.getElementById("statCourts").textContent = state.courts.length;

  let bookedCount = 0;
  state.courts.forEach(c => {
    bookedCount += Object.keys(c.reservations || {}).length;
  });
  document.getElementById("statBookedSlots").textContent = bookedCount;

  document.getElementById("statHostelStudents").textContent = state.hostelStudents.length;

  const totalFines = state.users.reduce((sum, u) => sum + (u.fineBalance || 0), 0);
  document.getElementById("statTotalFines").textContent = "Rs. " + totalFines.toFixed(0);
}

// ==========================================
// Rendering: Sports Courts & Slots
// ==========================================
function renderCourts() {
  const container = document.getElementById("courtsGrid");
  container.innerHTML = "";

  state.courts.forEach(court => {
    const card = document.createElement("div");
    card.className = "court-card";

    let courtIcon = "🏸";
    if (court.courtType.toLowerCase().includes("tennis")) courtIcon = "🎾";
    if (court.courtType.toLowerCase().includes("basket")) courtIcon = "🏀";

    const resMap = court.reservations || {};

    let slotsHtml = "";
    SLOTS.forEach(slot => {
      const reservation = resMap[slot];
      if (reservation) {
        slotsHtml += `
          <button class="slot-btn booked" onclick="handleSlotClick('${court.courtId}', '${slot}', true, '${reservation.userId || ''}')" title="Click to cancel booking">
            <span class="slot-time">${slot}</span>
            <span class="slot-status">🔴 ${reservation.name}</span>
          </button>
        `;
      } else {
        slotsHtml += `
          <button class="slot-btn available" onclick="handleSlotClick('${court.courtId}', '${slot}', false)" title="Click to reserve for ${getActiveUserName()}">
            <span class="slot-time">${slot}</span>
            <span class="slot-status">🟢 Available</span>
          </button>
        `;
      }
    });

    card.innerHTML = `
      <div class="court-header">
        <div class="court-type">
          <span>${courtIcon}</span> ${court.courtType} Court
        </div>
        <span class="court-id-badge">${court.courtId}</span>
      </div>
      <div class="slots-grid">
        ${slotsHtml}
      </div>
    `;

    container.appendChild(card);
  });
}

function getActiveUserName() {
  const u = state.users.find(x => x.userId === state.selectedUserId);
  return u ? u.name : "Current User";
}

async function handleSlotClick(courtId, slot, isBooked, bookedUserId) {
  if (!state.selectedUserId) {
    showToast("Please select an active user first.", "error");
    return;
  }

  if (isBooked) {
    // Attempt cancellation
    const isOwner = bookedUserId && bookedUserId.toLowerCase() === state.selectedUserId.toLowerCase();
    const promptMsg = isOwner
      ? `Cancel your reservation for slot '${slot}' on court ${courtId}?`
      : `Slot '${slot}' was booked by another user. Release it as Admin Override or current user?`;

    if (!confirm(promptMsg)) return;

    try {
      const res = await fetch("/api/release", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          courtId: courtId,
          slot: slot,
          userId: state.selectedUserId
        })
      });
      const data = await res.json();
      if (data.success) {
        showToast(data.message, "success");
        loadData();
      } else {
        showToast(data.error, "error");
      }
    } catch (err) {
      showToast("Network error: " + err.message, "error");
    }
  } else {
    // Attempt reservation
    try {
      const res = await fetch("/api/reserve", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          courtId: courtId,
          slot: slot,
          userId: state.selectedUserId
        })
      });
      const data = await res.json();
      if (data.success) {
        showToast(data.message, "success");
        loadData();
      } else {
        showToast(data.error, "error");
      }
    } catch (err) {
      showToast("Network error: " + err.message, "error");
    }
  }
}

// ==========================================
// Rendering: Hostel & Billing
// ==========================================
function renderHostelStudents() {
  const container = document.getElementById("hostelStudentsList");
  container.innerHTML = "";

  if (state.hostelStudents.length === 0) {
    container.innerHTML = `<p class="text-secondary">No hostel students registered yet.</p>`;
    return;
  }

  state.hostelStudents.forEach(hs => {
    const card = document.createElement("div");
    card.className = "student-card" + (hs.rollNo === state.selectedStudentRoll ? " selected" : "");
    card.onclick = () => {
      state.selectedStudentRoll = hs.rollNo;
      renderHostelStudents();
      fetchAndRenderBill(hs.rollNo);
    };

    card.innerHTML = `
      <div>
        <div class="student-meta-name">${hs.name} <span class="court-id-badge">${hs.rollNo}</span></div>
        <div class="student-meta-sub">
          <span class="badge badge-room">Room ${hs.roomNumber} (${hs.roomType})</span>
          <span class="badge badge-meal">${hs.mealPlan}</span>
          <span>📅 ${hs.leavesThisMonth} leaves</span>
        </div>
      </div>
      <div class="student-actions" onclick="event.stopPropagation()">
        <button class="btn btn-secondary btn-sm" onclick="openLeaveModal('${hs.rollNo}', '${hs.name}')">
          Apply Leave
        </button>
      </div>
    `;

    container.appendChild(card);
  });

  // Auto-select first student if none selected
  if (!state.selectedStudentRoll && state.hostelStudents.length > 0) {
    state.selectedStudentRoll = state.hostelStudents[0].rollNo;
    fetchAndRenderBill(state.selectedStudentRoll);
  }
}

async function fetchAndRenderBill(rollNo) {
  try {
    const res = await fetch(`/api/hostel/bill?rollNo=${encodeURIComponent(rollNo)}&monthDays=30`);
    if (!res.ok) return;
    const bill = await res.json();

    document.getElementById("billPlaceholder").classList.add("hidden");
    const container = document.getElementById("billContainer");
    container.classList.remove("hidden");

    container.innerHTML = `
      <div class="invoice">
        <div class="invoice-row">
          <span>Student Name / Roll:</span>
          <strong>${bill.name} (${bill.rollNo})</strong>
        </div>
        <div class="invoice-row">
          <span>Room Model:</span>
          <span>${bill.roomType} (Room ${bill.roomNumber})</span>
        </div>
        <div class="invoice-row">
          <span>Room Charges:</span>
          <strong>Rs. ${bill.roomCost.toFixed(2)}</strong>
        </div>
        <div class="invoice-row">
          <span>Meal Plan:</span>
          <span>${bill.mealPlan}</span>
        </div>
        <div class="invoice-row">
          <span>Month Cycle / Leaves:</span>
          <span>${bill.monthDays} days total - ${bill.leavesThisMonth} leaves deducted</span>
        </div>
        <div class="invoice-row">
          <span>Mess Cost (Billed Days):</span>
          <strong>Rs. ${bill.messCost.toFixed(2)}</strong>
        </div>
        <div class="invoice-row total-row">
          <span>Total Monthly Amount Due:</span>
          <span>Rs. ${bill.totalDue.toFixed(2)}</span>
        </div>
      </div>
      <div class="invoice-controls">
        <button class="btn btn-secondary btn-sm" onclick="window.print()">
          🖨️ Print Receipt
        </button>
        <button class="btn btn-secondary btn-sm" onclick="openLeaveModal('${bill.rollNo}', '${bill.name}')">
          ➕ Add Leave Days
        </button>
      </div>
    `;
  } catch (err) {
    console.error("Error fetching bill", err);
  }
}

function openLeaveModal(rollNo, name) {
  state.activeLeaveTargetStudent = rollNo;
  document.getElementById("leaveModalUserDesc").textContent = `Applying leaves for: ${name} (${rollNo})`;
  document.getElementById("leaveDaysInput").value = "1";
  openModal("leaveModal");
}

async function handleSubmitLeave() {
  const days = document.getElementById("leaveDaysInput").value;
  if (!days || days < 1) {
    showToast("Please enter a valid number of days.", "error");
    return;
  }

  try {
    const res = await fetch("/api/hostel/leave", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        rollNo: state.activeLeaveTargetStudent,
        days: days,
        monthDays: "30"
      })
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message, "success");
      closeModal("leaveModal");
      loadData();
    } else {
      showToast(data.error, "error");
    }
  } catch (err) {
    showToast("Network error: " + err.message, "error");
  }
}

async function handleRegisterHostelStudent() {
  const roll = document.getElementById("regRoll").value.trim();
  const name = document.getElementById("regName").value.trim();
  const roomType = document.getElementById("regRoomType").value;
  const roomNum = document.getElementById("regRoomNum").value.trim();
  const baseRate = document.getElementById("regBaseRate").value.trim();
  const mealPlan = document.getElementById("regMealPlan").value;

  if (!roll || !name || !roomNum) {
    showToast("Please fill in Roll No, Name, and Room Number.", "error");
    return;
  }

  try {
    const res = await fetch("/api/hostel/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        rollNo: roll,
        name: name,
        roomType: roomType,
        roomNumber: roomNum,
        baseRate: baseRate,
        mealPlan: mealPlan
      })
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message, "success");
      closeModal("registerModal");
      state.selectedStudentRoll = roll;
      loadData();
    } else {
      showToast(data.error, "error");
    }
  } catch (err) {
    showToast("Network error: " + err.message, "error");
  }
}

// ==========================================
// Rendering: Users & Fines
// ==========================================
function renderUsersGrid() {
  const container = document.getElementById("usersGrid");
  container.innerHTML = "";

  state.users.forEach(u => {
    const card = document.createElement("div");
    card.className = "user-card";

    let roleBadgeClass = "badge-student";
    if (u.role === "Faculty") roleBadgeClass = "badge-faculty";
    if (u.role === "Coach") roleBadgeClass = "badge-coach";

    const hasFine = u.fineBalance > 0;

    card.innerHTML = `
      <div class="user-card-header">
        <div>
          <div class="user-name">${u.name}</div>
          <div class="user-id-code">${u.userId}</div>
        </div>
        <span class="badge ${roleBadgeClass}">${u.role}</span>
      </div>
      <div class="user-meta-list">
        <div>Max Slot Limit: <strong>${u.bookingLimit} concurrent</strong></div>
        <div>Outstanding Fine: <span class="${hasFine ? 'fine-pill-due' : 'fine-pill-ok'}">Rs. ${u.fineBalance.toFixed(2)}</span></div>
      </div>
      <div class="user-card-actions">
        <button class="btn btn-secondary btn-sm" onclick="openFineModal('${u.userId}', '${u.name}', 'pay')">
          Pay Fine
        </button>
        <button class="btn btn-secondary btn-sm" onclick="openFineModal('${u.userId}', '${u.name}', 'add')">
          Add Fine
        </button>
      </div>
    `;

    container.appendChild(card);
  });
}

function openFineModal(userId, name, defaultAction) {
  state.activeFineTargetUser = userId;
  document.getElementById("fineModalTitle").textContent = `Manage Fine: ${name}`;
  document.getElementById("fineModalUserDesc").textContent = `User ID: ${userId} (${name})`;
  document.getElementById("fineAction").value = defaultAction;
  document.getElementById("fineAmount").value = "";
  openModal("fineModal");
}

async function handleSubmitFine() {
  const action = document.getElementById("fineAction").value;
  const amount = document.getElementById("fineAmount").value;

  if (!amount || amount <= 0) {
    showToast("Please enter a valid positive amount.", "error");
    return;
  }

  try {
    const res = await fetch("/api/fine", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        userId: state.activeFineTargetUser,
        action: action,
        amount: amount
      })
    });
    const data = await res.json();
    if (data.success) {
      showToast(`Fine updated! New balance: Rs. ${data.fineBalance.toFixed(2)}`, "success");
      closeModal("fineModal");
      loadData();
    } else {
      showToast(data.error, "error");
    }
  } catch (err) {
    showToast("Network error: " + err.message, "error");
  }
}

// ==========================================
// Save State
// ==========================================
async function saveSystemState() {
  try {
    const res = await fetch("/api/save", { method: "POST" });
    const data = await res.json();
    if (data.success) {
      showToast("State successfully persisted to disk!", "success");
    } else {
      showToast(data.error, "error");
    }
  } catch (err) {
    showToast("Network error: " + err.message, "error");
  }
}

// ==========================================
// Modals & Toast Utilities
// ==========================================
function openModal(id) {
  document.getElementById(id).classList.remove("hidden");
}

function closeModal(id) {
  document.getElementById(id).classList.add("hidden");
}

let toastTimeout = null;
function showToast(message, type = "success") {
  const toast = document.getElementById("toast");
  toast.textContent = (type === "success" ? "✅ " : "⚠️ ") + message;
  toast.className = `toast toast-${type}`;
  toast.classList.remove("hidden");

  if (toastTimeout) clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => {
    toast.classList.add("hidden");
  }, 4000);
}
