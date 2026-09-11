/* =========================================================
   Campus Academic Systems - Frontend Dashboard Logic
   ========================================================= */

const SLOTS = [
  "06:00-07:00", "07:00-08:00", "08:00-09:00",
  "09:00-10:00", "10:00-11:00", "16:00-17:00",
  "17:00-18:00", "18:00-19:00", "19:00-20:00"
];

const SEED_DATA = {
  users: [
    { userId: "S101", name: "Rahul Sharma", role: "Student", bookingLimit: 2, fineBalance: 0.0 },
    { userId: "S102", name: "Priya Nair", role: "Student", bookingLimit: 2, fineBalance: 0.0 },
    { userId: "F201", name: "Dr. Suresh Kumar", role: "Faculty", bookingLimit: 5, fineBalance: 0.0 },
    { userId: "C301", name: "Coach Vikram", role: "Coach", bookingLimit: 10, fineBalance: 0.0 }
  ],
  courts: [
    { courtId: "CRT1", courtType: "Badminton", reservations: {} },
    { courtId: "CRT2", courtType: "Tennis", reservations: {} },
    { courtId: "CRT3", courtType: "Basketball", reservations: {} }
  ],
  hostelStudents: [
    { rollNo: "STU202", name: "Anjali Menon", roomNumber: 302, roomType: "SingleOccupancy", roomTariff: 5400.0, mealPlan: "StandardPlan", leavesThisMonth: 0, baseRate: 4500.0 },
    { rollNo: "STU203", name: "Rohan Das", roomNumber: 101, roomType: "ACSuite", roomTariff: 7500.0, mealPlan: "SpecialDietPlan", leavesThisMonth: 0, baseRate: 6000.0 }
  ],
  books: [
    { isbn: "978-0262033848", title: "Introduction to Algorithms (CLRS)", author: "Thomas H. Cormen", category: "Computer Science", available: true, borrowerId: null, borrowerName: null },
    { isbn: "978-1118063330", title: "Operating System Concepts", author: "Abraham Silberschatz", category: "Systems", available: true, borrowerId: null, borrowerName: null },
    { isbn: "978-0078022159", title: "Database System Concepts", author: "Henry F. Korth", category: "Databases", available: true, borrowerId: null, borrowerName: null },
    { isbn: "978-0132126953", title: "Computer Networks", author: "Andrew S. Tanenbaum", category: "Networks", available: true, borrowerId: null, borrowerName: null },
    { isbn: "978-0132350884", title: "Clean Code: A Handbook of Agile Software Craftsmanship", author: "Robert C. Martin", category: "Software Engineering", available: true, borrowerId: null, borrowerName: null }
  ]
};

let state = {
  users: [],
  courts: [],
  hostelStudents: [],
  books: [],
  selectedUserId: null,
  selectedStudentRoll: null,
  activeFineTargetUser: null,
  activeLeaveTargetStudent: null,
  activeReturnBook: null,
  isStandaloneMode: false
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

  // Return Book Modal
  const returnBtn = document.getElementById("submitReturnBtn");
  if (returnBtn) returnBtn.addEventListener("click", handleSubmitReturnBook);
}

// ==========================================
// Data Fetching & Sync
// ==========================================
async function loadData() {
  const statusBadge = document.getElementById("serverStatusBadge");

  try {
    // Attempt connecting to Java backend
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 2500);

    const res = await fetch("/api/data", { signal: controller.signal });
    clearTimeout(timeoutId);

    if (!res.ok) throw new Error("Server returned HTTP " + res.status);
    const data = await res.json();

    state.isStandaloneMode = false;
    if (statusBadge) {
      statusBadge.textContent = "🟢 Live Java Backend";
      statusBadge.style.color = "var(--success)";
      statusBadge.title = "Connected to local Java Web Server";
    }

    state.users = data.users || [];
    state.courts = data.courts || [];
    state.hostelStudents = data.hostelStudents || [];
    state.books = data.books || [];

  } catch (err) {
    // Fallback to in-browser Standalone Web Demo Mode
    state.isStandaloneMode = true;
    if (statusBadge) {
      statusBadge.textContent = "🟡 Standalone Web Demo";
      statusBadge.style.color = "var(--warning)";
      statusBadge.title = "Running directly in browser with LocalStorage. Run WebLauncher for Java backend.";
    }

    loadLocalStandaloneData();
  }

  if (!state.selectedUserId && state.users.length > 0) {
    state.selectedUserId = state.users[0].userId;
  }

  renderUserSelector();
  renderStats();
  renderCourts();
  renderHostelStudents();
  renderLibraryCatalog();
  renderUsersGrid();

  if (state.selectedStudentRoll) {
    fetchAndRenderBill(state.selectedStudentRoll);
  }
}

function loadLocalStandaloneData() {
  const saved = localStorage.getItem("campus_standalone_data");
  if (saved) {
    try {
      const parsed = JSON.parse(saved);
      state.users = parsed.users || SEED_DATA.users;
      state.courts = parsed.courts || SEED_DATA.courts;
      state.hostelStudents = parsed.hostelStudents || SEED_DATA.hostelStudents;
      state.books = parsed.books || SEED_DATA.books;
      return;
    } catch (e) {}
  }
  // Initialize from seed
  state.users = JSON.parse(JSON.stringify(SEED_DATA.users));
  state.courts = JSON.parse(JSON.stringify(SEED_DATA.courts));
  state.hostelStudents = JSON.parse(JSON.stringify(SEED_DATA.hostelStudents));
  state.books = JSON.parse(JSON.stringify(SEED_DATA.books));
  saveLocalStandaloneData();
}

function saveLocalStandaloneData() {
  if (state.isStandaloneMode) {
    localStorage.setItem("campus_standalone_data", JSON.stringify({
      users: state.users,
      courts: state.courts,
      hostelStudents: state.hostelStudents,
      books: state.books
    }));
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

  const booksElem = document.getElementById("statLibraryBooks");
  if (booksElem) {
    booksElem.textContent = state.books.filter(b => b.available).length;
  }

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

  const currentUser = state.users.find(u => u.userId === state.selectedUserId);

  if (isBooked) {
    const isOwner = bookedUserId && bookedUserId.toLowerCase() === state.selectedUserId.toLowerCase();
    const promptMsg = isOwner
      ? `Cancel your reservation for slot '${slot}' on court ${courtId}?`
      : `Slot '${slot}' was booked by another user. Release it as Admin Override or current user?`;

    if (!confirm(promptMsg)) return;

    if (state.isStandaloneMode) {
      const targetCourt = state.courts.find(c => c.courtId === courtId);
      if (targetCourt && targetCourt.reservations) {
        delete targetCourt.reservations[slot];
        saveLocalStandaloneData();
        showToast(`Slot '${slot}' released successfully on ${courtId}.`, "success");
        loadData();
      }
      return;
    }

    try {
      const res = await fetch("/api/release", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ courtId, slot, userId: state.selectedUserId })
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
    if (state.isStandaloneMode) {
      // 1. Check fine
      if (currentUser.fineBalance > 0) {
        showToast(`Outstanding fine hold: ${currentUser.name} has unpaid fines of Rs. ${currentUser.fineBalance.toFixed(2)}. Reservations blocked!`, "error");
        return;
      }
      // 2. Check quota
      let userBookings = 0;
      state.courts.forEach(c => {
        Object.values(c.reservations || {}).forEach(r => {
          if (r && r.userId === currentUser.userId) userBookings++;
        });
      });
      if (userBookings >= currentUser.bookingLimit) {
        showToast(`Quota exceeded: ${currentUser.name} has reached maximum booking limit (${userBookings}/${currentUser.bookingLimit}).`, "error");
        return;
      }
      // Reserve
      const targetCourt = state.courts.find(c => c.courtId === courtId);
      if (!targetCourt.reservations) targetCourt.reservations = {};
      targetCourt.reservations[slot] = { userId: currentUser.userId, name: currentUser.name };
      saveLocalStandaloneData();
      showToast(`Slot '${slot}' reserved successfully for ${currentUser.name}!`, "success");
      loadData();
      return;
    }

    try {
      const res = await fetch("/api/reserve", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ courtId, slot, userId: state.selectedUserId })
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

  if (!state.selectedStudentRoll && state.hostelStudents.length > 0) {
    state.selectedStudentRoll = state.hostelStudents[0].rollNo;
    fetchAndRenderBill(state.selectedStudentRoll);
  }
}

async function fetchAndRenderBill(rollNo) {
  let bill = null;

  if (state.isStandaloneMode) {
    const student = state.hostelStudents.find(s => s.rollNo === rollNo);
    if (!student) return;

    const baseRate = student.baseRate || 4500.0;
    const roomCost = student.roomType === "ACSuite" ? baseRate + 1500.0 : baseRate * 1.2;
    const dailyMessRate = student.mealPlan === "SpecialDietPlan" ? 150.0 : 120.0;
    const activeMessDays = Math.max(0, 30 - student.leavesThisMonth);
    let messCost = activeMessDays * dailyMessRate;
    if (student.mealPlan === "SpecialDietPlan") messCost += 500.0;

    bill = {
      rollNo: student.rollNo,
      name: student.name,
      roomType: student.roomType,
      roomNumber: student.roomNumber,
      roomCost: roomCost,
      mealPlan: student.mealPlan,
      leavesThisMonth: student.leavesThisMonth,
      monthDays: 30,
      messCost: messCost,
      totalDue: roomCost + messCost
    };
  } else {
    try {
      const res = await fetch(`/api/hostel/bill?rollNo=${encodeURIComponent(rollNo)}&monthDays=30`);
      if (res.ok) {
        bill = await res.json();
      }
    } catch (err) {}
  }

  if (!bill) return;

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
}

function openLeaveModal(rollNo, name) {
  state.activeLeaveTargetStudent = rollNo;
  document.getElementById("leaveModalUserDesc").textContent = `Applying leaves for: ${name} (${rollNo})`;
  document.getElementById("leaveDaysInput").value = "1";
  openModal("leaveModal");
}

async function handleSubmitLeave() {
  const daysInput = document.getElementById("leaveDaysInput").value;
  const days = parseInt(daysInput, 10);
  if (isNaN(days) || days < 1) {
    showToast("Please enter a valid number of days (at least 1).", "error");
    return;
  }

  if (state.isStandaloneMode) {
    const student = state.hostelStudents.find(s => s.rollNo === state.activeLeaveTargetStudent);
    if (!student) return;
    if (student.leavesThisMonth + days > 30) {
      showToast(`Invalid leave: Total leaves (${student.leavesThisMonth + days}) cannot exceed month days (30).`, "error");
      return;
    }
    student.leavesThisMonth += days;
    saveLocalStandaloneData();
    showToast(`Applied ${days} days leave for ${student.name}.`, "success");
    closeModal("leaveModal");
    loadData();
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
  const roomNum = parseInt(document.getElementById("regRoomNum").value.trim(), 10);
  const baseRate = parseFloat(document.getElementById("regBaseRate").value.trim()) || 4500.0;
  const mealPlan = document.getElementById("regMealPlan").value;

  if (!roll || !name || isNaN(roomNum)) {
    showToast("Please fill in Roll No, Name, and Room Number.", "error");
    return;
  }

  if (state.isStandaloneMode) {
    const exists = state.hostelStudents.some(s => s.rollNo.toLowerCase() === roll.toLowerCase());
    if (exists) {
      showToast(`Student with roll ${roll} is already registered.`, "error");
      return;
    }
    const roomTariff = roomType === "ACSuite" ? baseRate + 1500.0 : baseRate * 1.2;
    state.hostelStudents.push({
      rollNo: roll,
      name: name,
      roomNumber: roomNum,
      roomType: roomType,
      roomTariff: roomTariff,
      mealPlan: mealPlan,
      leavesThisMonth: 0,
      baseRate: baseRate
    });
    saveLocalStandaloneData();
    showToast(`Registered hostel student ${name} (${roll})!`, "success");
    closeModal("registerModal");
    state.selectedStudentRoll = roll;
    loadData();
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
// Rendering: Campus Library
// ==========================================
function renderLibraryCatalog() {
  const container = document.getElementById("booksGrid");
  if (!container) return;
  container.innerHTML = "";

  if (state.books.length === 0) {
    container.innerHTML = `<p class="text-secondary">No books in catalog.</p>`;
    return;
  }

  state.books.forEach(b => {
    const card = document.createElement("div");
    card.className = "book-card " + (b.available ? "available" : "borrowed");

    let actionBtn = "";
    if (b.available) {
      actionBtn = `
        <button class="btn btn-primary btn-sm" onclick="handleBorrowBook('${b.isbn}')" title="Borrow for active user">
          📖 Borrow Book
        </button>
      `;
    } else {
      actionBtn = `
        <button class="btn btn-secondary btn-sm" onclick="openReturnModal('${b.isbn}', '${b.title.replace(/'/g, "\\'")}', '${(b.borrowerName || '').replace(/'/g, "\\'")}', '${b.borrowerId || ''}')">
          ↩️ Return / Settle
        </button>
      `;
    }

    card.innerHTML = `
      <div>
        <div class="book-header">
          <div>
            <div class="book-title">${b.title}</div>
            <div class="book-author">by ${b.author}</div>
          </div>
          <span class="book-category-badge">${b.category}</span>
        </div>
        <div class="book-isbn">ISBN: ${b.isbn}</div>
      </div>
      <div class="book-status-row">
        <div>
          ${b.available 
            ? '<span style="color: var(--success); font-weight: 600;">🟢 Available</span>' 
            : `<span style="color: var(--warning); font-weight: 600;">🔴 Loaned to ${b.borrowerName}</span>`}
        </div>
        ${actionBtn}
      </div>
    `;

    container.appendChild(card);
  });
}

async function handleBorrowBook(isbn) {
  if (!state.selectedUserId) {
    showToast("Please select an active user first.", "error");
    return;
  }

  const currentUser = state.users.find(u => u.userId === state.selectedUserId);

  if (state.isStandaloneMode) {
    const book = state.books.find(b => b.isbn === isbn);
    if (!book || !book.available) {
      showToast("Book is not currently available.", "error");
      return;
    }
    if (currentUser.fineBalance > 0) {
      showToast(`Library hold: ${currentUser.name} has unpaid fines of Rs. ${currentUser.fineBalance.toFixed(2)}. Borrowing blocked!`, "error");
      return;
    }
    const userLoans = state.books.filter(b => !b.available && b.borrowerId === currentUser.userId).length;
    const maxLoans = currentUser.role === "Student" ? 3 : 10;
    if (userLoans >= maxLoans) {
      showToast(`Borrowing quota exceeded: ${currentUser.name} already has ${userLoans} active book loans (Limit: ${maxLoans}).`, "error");
      return;
    }
    book.available = false;
    book.borrowerId = currentUser.userId;
    book.borrowerName = currentUser.name;
    saveLocalStandaloneData();
    showToast(`Book '${book.title}' checked out to ${currentUser.name}!`, "success");
    loadData();
    return;
  }

  try {
    const res = await fetch("/api/library/borrow", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ isbn, userId: state.selectedUserId })
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

function openReturnModal(isbn, title, borrowerName, borrowerId) {
  state.activeReturnBook = { isbn, title, borrowerName, borrowerId };
  document.getElementById("returnModalTitle").textContent = `Return: ${title}`;
  document.getElementById("returnModalBookDesc").textContent = `ISBN: ${isbn} - ${title}`;
  document.getElementById("returnModalBorrowerDesc").textContent = `Borrower: ${borrowerName} (${borrowerId})`;
  document.getElementById("returnOverdueDays").value = "0";
  openModal("returnModal");
}

async function handleSubmitReturnBook() {
  if (!state.activeReturnBook) return;

  const daysInput = document.getElementById("returnOverdueDays").value;
  const days = Math.max(0, parseInt(daysInput, 10) || 0);

  if (state.isStandaloneMode) {
    const book = state.books.find(b => b.isbn === state.activeReturnBook.isbn);
    if (!book) return;
    const borrower = state.users.find(u => u.userId === state.activeReturnBook.borrowerId);
    book.available = true;
    book.borrowerId = null;
    book.borrowerName = null;

    const fineCharged = days * 5.0;
    if (fineCharged > 0 && borrower) {
      borrower.fineBalance += fineCharged;
    }
    saveLocalStandaloneData();
    closeModal("returnModal");

    if (fineCharged > 0 && borrower) {
      showToast(`Book returned! Overdue fine of Rs. ${fineCharged.toFixed(2)} added to ${borrower.name}'s campus account. (Sports court bookings now locked until paid!)`, "error");
    } else {
      showToast(`Book '${book.title}' successfully returned.`, "success");
    }
    loadData();
    return;
  }

  try {
    const res = await fetch("/api/library/return", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        isbn: state.activeReturnBook.isbn,
        overdueDays: days
      })
    });
    const data = await res.json();
    if (data.success) {
      closeModal("returnModal");
      if (data.fineCharged > 0) {
        showToast(`Book returned! Overdue penalty of Rs. ${data.fineCharged.toFixed(2)} added to ${state.activeReturnBook.borrowerName}'s campus account. (Sports court bookings now locked until cleared!)`, "error");
      } else {
        showToast(data.message, "success");
      }
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
  const amountInput = document.getElementById("fineAmount").value;
  const amount = parseFloat(amountInput);

  if (isNaN(amount) || amount <= 0) {
    showToast("Please enter a valid positive amount.", "error");
    return;
  }

  if (state.isStandaloneMode) {
    const user = state.users.find(u => u.userId === state.activeFineTargetUser);
    if (user) {
      if (action === "add") {
        user.fineBalance += amount;
      } else {
        user.fineBalance = Math.max(0, user.fineBalance - amount);
      }
      saveLocalStandaloneData();
      showToast(`Fine updated! New balance for ${user.name}: Rs. ${user.fineBalance.toFixed(2)}`, "success");
      closeModal("fineModal");
      loadData();
    }
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
  if (state.isStandaloneMode) {
    saveLocalStandaloneData();
    showToast("State persisted to browser storage! (To save to .ser file, run WebLauncher).", "success");
    return;
  }

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
  }, 4500);
}
