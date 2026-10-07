// Sports & Facilities booking — multi-player team reservations, reschedule & release logic.

const FACILITIES = [
  { id: 'turf',      name: 'Football Turf',       ic: '⚽', location: 'Main Ground',        type: 'Outdoor · synthetic turf (11+11 Match / 7+7 Turf)' },
  { id: 'cricket',   name: 'Cricket Ground',      ic: '🏏', location: 'South Sports Arena',  type: 'Outdoor · turf pitch (11+11 Match / Nets)' },
  { id: 'badminton', name: 'Badminton Court',      ic: '🏸', location: 'Indoor Sports Hall',  type: 'Indoor · wooden court, AC (Singles / Doubles)' },
  { id: 'tennis',    name: 'Tennis Court',        ic: '🎾', location: 'East Courts',        type: 'Outdoor · clay court (Singles / Doubles)' },
  { id: 'bball',     name: 'Basketball Court A', ic: '🏀', location: 'North Sports Block', type: 'Outdoor · hard court (5v5 Match / 3v3)' },
];

const COURT_MAPPING = {
  'badminton': 'CRT1',
  'tennis': 'CRT2',
  'bball': 'CRT3',
  'turf': 'CRT4',
  'cricket': 'CRT5'
};

const SPORT_RULES = {
  'turf': {
    formats: [
      { id: '7 vs 7 Turf Match', label: '7 vs 7 Turf Match', required: 14 },
      { id: '11 vs 11 Match', label: '11 vs 11 Full Match', required: 22 }
    ],
    defaultFormat: '7 vs 7 Turf Match'
  },
  'cricket': {
    formats: [
      { id: 'Nets Practice Session', label: 'Nets Practice Session', required: 4 },
      { id: '11 vs 11 Match', label: '11 vs 11 Match (Full Teams)', required: 22 }
    ],
    defaultFormat: 'Nets Practice Session'
  },
  'badminton': {
    formats: [
      { id: 'Singles (1 vs 1)', label: 'Singles (1 vs 1)', required: 2 },
      { id: 'Doubles (2 vs 2)', label: 'Doubles (2 vs 2)', required: 4 }
    ],
    defaultFormat: 'Singles (1 vs 1)'
  },
  'tennis': {
    formats: [
      { id: 'Singles (1 vs 1)', label: 'Singles (1 vs 1)', required: 2 },
      { id: 'Doubles (2 vs 2)', label: 'Doubles (2 vs 2)', required: 4 }
    ],
    defaultFormat: 'Singles (1 vs 1)'
  },
  'bball': {
    formats: [
      { id: '3 vs 3 Half Court', label: '3 vs 3 Half Court', required: 6 },
      { id: '5 vs 5 Full Match', label: '5 vs 5 Full Match', required: 10 }
    ],
    defaultFormat: '3 vs 3 Half Court'
  }
};

const SLOT_TIMES = ['06:00','07:00','08:00','09:00','10:00','11:00','12:00','13:00','14:00','15:00','16:00','17:00','18:00','19:00','20:00','21:00'];
const BOOKINGS_KEY = 'ridgeview_sports_bookings';

function getNextDates(count) {
  const days = [];
  for (let i = 0; i < count; i++) {
    const d = new Date();
    d.setDate(d.getDate() + i);
    days.push(d);
  }
  return days;
}

function dateKey(d) {
  return d.toISOString().slice(0, 10);
}

function loadBookings() {
  try {
    return JSON.parse(localStorage.getItem(BOOKINGS_KEY)) || {};
  } catch (e) {
    return {};
  }
}

function saveBookings(data) {
  localStorage.setItem(BOOKINGS_KEY, JSON.stringify(data));
}

function seedHash(str) {
  let h = 0;
  for (let i = 0; i < str.length; i++) h = (h * 31 + str.charCodeAt(i)) >>> 0;
  return h;
}

function slotKey(facilityId, dKey, time) {
  return `${facilityId}|${dKey}|${time}`;
}

function getSlotState(facilityId, dKey, time, isToday, currentHour) {
  const bookings = loadBookings();
  const key = slotKey(facilityId, dKey, time);
  if (bookings[key]) return bookings[key].by === 'you' ? 'booked-you' : 'booked-other';

  const hash = seedHash(key);
  if (hash % 6 === 0) return 'booked-other'; // realistic occupancy
  return 'free';
}

function getUserProfile() {
  const userProfileRaw = sessionStorage.getItem('ridgeview_profile');
  let userId = 'S101';
  let userName = 'Rahul Sharma';
  if (userProfileRaw) {
    try {
      const p = JSON.parse(userProfileRaw);
      userId = p.id || 'S101';
      userName = p.name || 'Rahul Sharma';
    } catch(e) {}
  }
  return { userId, userName };
}

// Global modal variables
let pendingSlot = null; // { facilityId, date, time, courtId, slotStr }
let modifyingBooking = null; // booking object being edited

/* ----------------------------------------------------------------
   Modal Controller Functions
------------------------------------------------------------------*/
function openTeamBookingModal(facilityId, dKey, time) {
  const modal = document.getElementById('teamBookingModal');
  if (!modal) return;

  const f = FACILITIES.find(x => x.id === facilityId);
  const endHour = String(parseInt(time, 10) + 1).padStart(2, '0');
  const slotStr = `${time}-${endHour}:00`;
  const courtId = COURT_MAPPING[facilityId] || 'CRT1';
  const { userId, userName } = getUserProfile();

  pendingSlot = { facilityId, dKey, time, courtId, slotStr, userId, userName };

  document.getElementById('tbmTitle').textContent = `Reserve ${f.name}`;
  document.getElementById('tbmFacilityBadge').textContent = `${f.ic} ${f.name} · ${dKey} · ${slotStr}`;
  document.getElementById('tbmCaptainDisplay').textContent = `${userName} (${userId})`;

  const formatSelect = document.getElementById('tbmFormatSelect');
  const rules = SPORT_RULES[facilityId] || SPORT_RULES['badminton'];
  formatSelect.innerHTML = rules.formats.map(fmt => 
    `<option value="${fmt.id}" data-req="${fmt.required}">${fmt.label} (${fmt.required} players required)</option>`
  ).join('');

  updateRequirementBadge();
  document.getElementById('tbmErrorAlert').style.display = 'none';

  // Pre-fill captain in roster
  document.getElementById('tbmPlayerInput').value = userId;

  modal.classList.remove('hidden');
}

function closeTeamBookingModal() {
  const modal = document.getElementById('teamBookingModal');
  if (modal) modal.classList.add('hidden');
  pendingSlot = null;
}

function updateRequirementBadge() {
  const select = document.getElementById('tbmFormatSelect');
  const badge = document.getElementById('tbmRequirementBadge');
  if (!select || !badge) return;
  const opt = select.options[select.selectedIndex];
  const req = opt ? opt.getAttribute('data-req') : '2';
  badge.textContent = `Minimum ${req} Players Required`;
}

function autofillSquad(inputId, selectId) {
  const select = document.getElementById(selectId);
  const opt = select.options[select.selectedIndex];
  const req = opt ? parseInt(opt.getAttribute('data-req'), 10) : 2;
  const { userId } = getUserProfile();

  const squad = [userId];
  for (let i = 2; i <= req; i++) {
    squad.push(`S10${i > 9 ? i : '0' + i}`);
  }
  document.getElementById(inputId).value = squad.join(', ');
}

function openModifyBookingModal(booking) {
  const modal = document.getElementById('modifyBookingModal');
  if (!modal) return;

  modifyingBooking = booking;
  const f = FACILITIES.find(x => x.id === booking.facility) || { name: booking.facility, ic: '🏟️' };
  const endHour = String(parseInt(booking.time, 10) + 1).padStart(2, '0');

  document.getElementById('mbmCurrentDetails').innerHTML = `
    <strong>${f.ic} ${f.name}</strong><br>
    Date: ${booking.date} | Current Slot: <strong>${booking.time}–${endHour}:00</strong><br>
    Format: <strong>${booking.format || 'Standard'}</strong> | Players: <strong>${booking.players ? booking.players.length : 1} registered</strong>
  `;

  // Populate new slots dropdown
  const slotSelect = document.getElementById('mbmNewSlotSelect');
  slotSelect.innerHTML = SLOT_TIMES
    .filter(t => t !== booking.time)
    .map(t => {
      const eH = String(parseInt(t, 10) + 1).padStart(2, '0');
      return `<option value="${t}-${eH}:00">${t}–${eH}:00</option>`;
    }).join('');

  // Populate format dropdown
  const formatSelect = document.getElementById('mbmFormatSelect');
  const rules = SPORT_RULES[booking.facility] || SPORT_RULES['badminton'];
  formatSelect.innerHTML = rules.formats.map(fmt => 
    `<option value="${fmt.id}" data-req="${fmt.required}" ${booking.format === fmt.id ? 'selected' : ''}>${fmt.label} (${fmt.required} players required)</option>`
  ).join('');

  document.getElementById('mbmPlayerInput').value = (booking.players && booking.players.length > 0) 
    ? booking.players.join(', ') 
    : booking.userId;

  updateModifyReqBadge();
  document.getElementById('mbmErrorAlert').style.display = 'none';
  switchModifyTab('reschedule');

  modal.classList.remove('hidden');
}

function closeModifyBookingModal() {
  const modal = document.getElementById('modifyBookingModal');
  if (modal) modal.classList.add('hidden');
  modifyingBooking = null;
}

function switchModifyTab(tab) {
  const rSec = document.getElementById('mbmRescheduleSection');
  const pSec = document.getElementById('mbmRosterSection');
  const rBtn = document.getElementById('mbmTabRescheduleBtn');
  const pBtn = document.getElementById('mbmTabRosterBtn');

  if (tab === 'reschedule') {
    rSec.style.display = 'block';
    pSec.style.display = 'none';
    rBtn.className = 'btn btn-sm btn-primary';
    pBtn.className = 'btn btn-sm btn-outline';
  } else {
    rSec.style.display = 'none';
    pSec.style.display = 'block';
    rBtn.className = 'btn btn-sm btn-outline';
    pBtn.className = 'btn btn-sm btn-primary';
  }
}

function updateModifyReqBadge() {
  const select = document.getElementById('mbmFormatSelect');
  const badge = document.getElementById('mbmReqBadge');
  if (!select || !badge) return;
  const opt = select.options[select.selectedIndex];
  const req = opt ? opt.getAttribute('data-req') : '2';
  badge.textContent = `Minimum ${req} Players Required`;
}

/* ----------------------------------------------------------------
   Sports Booking Core Engine
------------------------------------------------------------------*/
function initSportsBooking() {
  const facilityTabsEl = document.getElementById('facilityTabs');
  if (!facilityTabsEl) return;

  const facilityMetaEl = document.getElementById('facilityMeta');
  const dateTabsEl = document.getElementById('dateTabs');
  const slotGridEl = document.getElementById('slotGrid');
  const myBookingsEl = document.getElementById('myBookingsList');
  const toastEl = document.getElementById('bookingToast');

  const dates = getNextDates(7);
  let activeFacility = FACILITIES[0].id;
  let activeDate = dateKey(dates[0]);

  const dayLabel = (d) => d.toLocaleDateString(undefined, { weekday: 'short' });
  const dateLabel = (d) => d.getDate();

  function showToast(msg) {
    if (!toastEl) return;
    toastEl.textContent = msg;
    toastEl.classList.add('show');
    clearTimeout(showToast._t);
    showToast._t = setTimeout(() => toastEl.classList.remove('show'), 2800);
  }

  function renderFacilityTabs() {
    facilityTabsEl.innerHTML = FACILITIES.map(f => `
      <button type="button" class="facility-tab ${f.id === activeFacility ? 'active' : ''}" data-facility="${f.id}">
        <span class="ic">${f.ic}</span> ${f.name}
      </button>`).join('');
    facilityTabsEl.querySelectorAll('.facility-tab').forEach(btn => {
      btn.addEventListener('click', () => {
        activeFacility = btn.getAttribute('data-facility');
        renderFacilityTabs();
        renderMeta();
        renderSlots();
      });
    });
  }

  function renderMeta() {
    const f = FACILITIES.find(x => x.id === activeFacility);
    facilityMetaEl.innerHTML = `<strong>${f.name}</strong> — ${f.location} · ${f.type}`;
  }

  function renderDateTabs() {
    dateTabsEl.innerHTML = dates.map((d, i) => {
      const dk = dateKey(d);
      return `<button type="button" class="date-tab ${dk === activeDate ? 'active' : ''}" data-date="${dk}">
        ${i === 0 ? 'Today' : dayLabel(d)}<span class="d">${dateLabel(d)}</span>
      </button>`;
    }).join('');
    dateTabsEl.querySelectorAll('.date-tab').forEach(btn => {
      btn.addEventListener('click', () => {
        activeDate = btn.getAttribute('data-date');
        renderDateTabs();
        renderSlots();
      });
    });
  }

  function renderSlots() {
    const today = new Date();
    const isToday = activeDate === dateKey(today);
    const currentHour = today.getHours();

    slotGridEl.innerHTML = SLOT_TIMES.map(time => {
      const state = getSlotState(activeFacility, activeDate, time, isToday, currentHour);
      const endHour = String(parseInt(time, 10) + 1).padStart(2, '0');
      return `<button type="button" class="slot-btn ${state}" data-time="${time}">
        ${time}–${endHour}:00
      </button>`;
    }).join('');

    slotGridEl.querySelectorAll('.slot-btn').forEach(btn => {
      btn.addEventListener('click', () => handleSlotClick(btn.getAttribute('data-time')));
    });
  }

  function handleSlotClick(time) {
    const bookings = loadBookings();
    const key = slotKey(activeFacility, activeDate, time);

    if (bookings[key] && bookings[key].by === 'you') {
      // Clicked on own booked slot -> open modify modal
      openModifyBookingModal(bookings[key]);
    } else if (bookings[key] && bookings[key].by !== 'you') {
      showToast(`Slot ${time} is already booked by another student.`);
    } else {
      const hash = seedHash(key);
      if (hash % 6 === 0) {
        showToast(`Slot ${time} is already occupied by campus teams.`);
        return;
      }
      // Empty slot -> open team booking modal
      openTeamBookingModal(activeFacility, activeDate, time);
    }
  }

  // Handle Team Booking Modal Confirm
  const tbmConfirmBtn = document.getElementById('tbmConfirmBtn');
  if (tbmConfirmBtn) {
    tbmConfirmBtn.addEventListener('click', async () => {
      if (!pendingSlot) return;

      const formatSelect = document.getElementById('tbmFormatSelect');
      const format = formatSelect.value;
      const opt = formatSelect.options[formatSelect.selectedIndex];
      const required = parseInt(opt.getAttribute('data-req'), 10);

      const rawInput = document.getElementById('tbmPlayerInput').value;
      const players = rawInput.split(',').map(s => s.trim()).filter(s => s.length > 0);

      const errorAlert = document.getElementById('tbmErrorAlert');

      if (players.length < required) {
        errorAlert.textContent = `Insufficient Players: ${format} requires at least ${required} students. You provided ${players.length}. Use "Auto-Fill Full Squad" to populate quickly.`;
        errorAlert.style.display = 'block';
        return;
      }

      // Call live backend
      try {
        const res = await fetch('/api/reserve', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            courtId: pendingSlot.courtId,
            slot: pendingSlot.slotStr,
            userId: pendingSlot.userId,
            format: format,
            players: players.join(',')
          })
        });
        const data = await res.json();
        if (!data.success && data.error) {
          errorAlert.textContent = data.error;
          errorAlert.style.display = 'block';
          return;
        }
      } catch (e) {
        // Fallback for offline demo
      }

      // Save locally
      const bookings = loadBookings();
      const key = slotKey(pendingSlot.facilityId, pendingSlot.dKey, pendingSlot.time);
      bookings[key] = {
        by: 'you',
        facility: pendingSlot.facilityId,
        courtId: pendingSlot.courtId,
        date: pendingSlot.dKey,
        time: pendingSlot.time,
        slotStr: pendingSlot.slotStr,
        userId: pendingSlot.userId,
        userName: pendingSlot.userName,
        format: format,
        players: players
      };
      saveBookings(bookings);

      closeTeamBookingModal();
      renderSlots();
      renderMyBookings();
      showToast(`Reserved ${pendingSlot.slotStr} (${format} · ${players.length} players)`);
    });
  }

  // Handle Reschedule Confirm
  const mbmConfirmRescheduleBtn = document.getElementById('mbmConfirmRescheduleBtn');
  if (mbmConfirmRescheduleBtn) {
    mbmConfirmRescheduleBtn.addEventListener('click', async () => {
      if (!modifyingBooking) return;

      const newSlotStr = document.getElementById('mbmNewSlotSelect').value;
      const oldSlotStr = modifyingBooking.slotStr || `${modifyingBooking.time}-${String(parseInt(modifyingBooking.time, 10)+1).padStart(2, '0')}:00`;
      const newTime = newSlotStr.split('-')[0];
      const errorAlert = document.getElementById('mbmErrorAlert');

      try {
        const res = await fetch('/api/modify', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            courtId: modifyingBooking.courtId,
            slot: oldSlotStr,
            newSlot: newSlotStr,
            userId: modifyingBooking.userId
          })
        });
        const data = await res.json();
        if (!data.success && data.error) {
          errorAlert.textContent = data.error;
          errorAlert.style.display = 'block';
          return;
        }
      } catch (e) {}

      // Update local ledger
      const bookings = loadBookings();
      const oldKey = slotKey(modifyingBooking.facility, modifyingBooking.date, modifyingBooking.time);
      delete bookings[oldKey];

      const newKey = slotKey(modifyingBooking.facility, modifyingBooking.date, newTime);
      modifyingBooking.time = newTime;
      modifyingBooking.slotStr = newSlotStr;
      bookings[newKey] = modifyingBooking;
      saveBookings(bookings);

      closeModifyBookingModal();
      renderSlots();
      renderMyBookings();
      showToast(`Rescheduled to ${newSlotStr} successfully.`);
    });
  }

  // Handle Roster Update Confirm
  const mbmConfirmRosterBtn = document.getElementById('mbmConfirmRosterBtn');
  if (mbmConfirmRosterBtn) {
    mbmConfirmRosterBtn.addEventListener('click', async () => {
      if (!modifyingBooking) return;

      const formatSelect = document.getElementById('mbmFormatSelect');
      const format = formatSelect.value;
      const opt = formatSelect.options[formatSelect.selectedIndex];
      const required = parseInt(opt.getAttribute('data-req'), 10);

      const rawInput = document.getElementById('mbmPlayerInput').value;
      const players = rawInput.split(',').map(s => s.trim()).filter(s => s.length > 0);
      const errorAlert = document.getElementById('mbmErrorAlert');

      if (players.length < required) {
        errorAlert.textContent = `Insufficient Players: ${format} requires at least ${required} students. Provided: ${players.length}.`;
        errorAlert.style.display = 'block';
        return;
      }

      const slotStr = modifyingBooking.slotStr || `${modifyingBooking.time}-${String(parseInt(modifyingBooking.time, 10)+1).padStart(2, '0')}:00`;

      try {
        const res = await fetch('/api/modify', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            courtId: modifyingBooking.courtId,
            slot: slotStr,
            userId: modifyingBooking.userId,
            format: format,
            players: players.join(',')
          })
        });
        const data = await res.json();
        if (!data.success && data.error) {
          errorAlert.textContent = data.error;
          errorAlert.style.display = 'block';
          return;
        }
      } catch (e) {}

      // Update local ledger
      const bookings = loadBookings();
      const key = slotKey(modifyingBooking.facility, modifyingBooking.date, modifyingBooking.time);
      if (bookings[key]) {
        bookings[key].format = format;
        bookings[key].players = players;
        saveBookings(bookings);
      }

      closeModifyBookingModal();
      renderMyBookings();
      showToast(`Updated team roster (${format} · ${players.length} players)`);
    });
  }

  // Handle Delete / Release in Modify Modal
  const mbmDeleteBookingBtn = document.getElementById('mbmDeleteBookingBtn');
  if (mbmDeleteBookingBtn) {
    mbmDeleteBookingBtn.addEventListener('click', async () => {
      if (!modifyingBooking) return;

      const slotStr = modifyingBooking.slotStr || `${modifyingBooking.time}-${String(parseInt(modifyingBooking.time, 10)+1).padStart(2, '0')}:00`;

      try {
        await fetch('/api/release', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            courtId: modifyingBooking.courtId,
            slot: slotStr,
            userId: modifyingBooking.userId
          })
        });
      } catch (e) {}

      const bookings = loadBookings();
      const key = slotKey(modifyingBooking.facility, modifyingBooking.date, modifyingBooking.time);
      delete bookings[key];
      saveBookings(bookings);

      closeModifyBookingModal();
      renderSlots();
      renderMyBookings();
      showToast('Reservation deleted / cancelled.');
    });
  }

  function renderMyBookings() {
    if (!myBookingsEl) return;
    const bookings = loadBookings();
    const mine = Object.values(bookings).filter(b => b.by === 'you')
      .sort((a, b) => (a.date + a.time).localeCompare(b.date + b.time));

    if (mine.length === 0) {
      myBookingsEl.innerHTML = `<p style="font-size:.85rem; color:var(--ink-soft); padding:12px 0;">You haven't reserved any slots yet — select a facility and available time above.</p>`;
      return;
    }

    myBookingsEl.innerHTML = mine.map(b => {
      const f = FACILITIES.find(x => x.id === b.facility) || { name: b.facility, ic: '🏟️' };
      const endHour = String(parseInt(b.time, 10) + 1).padStart(2, '0');
      const formatBadge = b.format ? `<span class="badge blue" style="font-size:.7rem; margin-left:6px;">${b.format} (${b.players ? b.players.length : 1}p)</span>` : '';
      return `<div class="notif-item" style="display:flex; justify-content:space-between; align-items:center; padding:12px; border-bottom:1px solid var(--line);">
        <div style="display:flex; align-items:center; gap:10px;">
          <div class="notif-dot"></div>
          <div>
            <p style="margin:0;"><strong>${f.ic} ${f.name}</strong> ${formatBadge}</p>
            <div class="notif-time" style="font-size:.78rem; color:var(--ink-soft);">${b.date} · ${b.time}–${endHour}:00</div>
          </div>
        </div>
        <div style="display:flex; gap:8px;">
          <button type="button" class="btn btn-outline btn-sm" data-mod-facility="${b.facility}" data-mod-date="${b.date}" data-mod-time="${b.time}">Modify</button>
          <button type="button" class="btn btn-outline btn-sm" style="color:var(--red); border-color:#fca5a5;" data-cancel-facility="${b.facility}" data-cancel-date="${b.date}" data-cancel-time="${b.time}">Cancel</button>
        </div>
      </div>`;
    }).join('');

    myBookingsEl.querySelectorAll('[data-mod-facility]').forEach(btn => {
      btn.addEventListener('click', () => {
        const fId = btn.getAttribute('data-mod-facility');
        const dStr = btn.getAttribute('data-mod-date');
        const tStr = btn.getAttribute('data-mod-time');
        const bk = loadBookings();
        const item = bk[slotKey(fId, dStr, tStr)];
        if (item) openModifyBookingModal(item);
      });
    });

    myBookingsEl.querySelectorAll('[data-cancel-facility]').forEach(btn => {
      btn.addEventListener('click', async () => {
        const fId = btn.getAttribute('data-cancel-facility');
        const dStr = btn.getAttribute('data-cancel-date');
        const tStr = btn.getAttribute('data-cancel-time');
        const bk = loadBookings();
        const item = bk[slotKey(fId, dStr, tStr)];
        if (!item) return;

        const slotStr = item.slotStr || `${tStr}-${String(parseInt(tStr, 10)+1).padStart(2, '0')}:00`;
        const courtId = COURT_MAPPING[fId] || 'CRT1';

        try {
          await fetch('/api/release', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ courtId: courtId, slot: slotStr, userId: item.userId })
          });
        } catch(e) {}

        delete bk[slotKey(fId, dStr, tStr)];
        saveBookings(bk);

        renderMyBookings();
        renderSlots();
        showToast('Reservation cancelled.');
      });
    });
  }

  // Setup Format change listeners
  const tbmFormatSelect = document.getElementById('tbmFormatSelect');
  if (tbmFormatSelect) {
    tbmFormatSelect.addEventListener('change', updateRequirementBadge);
  }
  const mbmFormatSelect = document.getElementById('mbmFormatSelect');
  if (mbmFormatSelect) {
    mbmFormatSelect.addEventListener('change', updateModifyReqBadge);
  }

  // Setup Autofill buttons
  const tbmAutofillBtn = document.getElementById('tbmAutofillBtn');
  if (tbmAutofillBtn) {
    tbmAutofillBtn.addEventListener('click', () => autofillSquad('tbmPlayerInput', 'tbmFormatSelect'));
  }
  const mbmAutofillBtn = document.getElementById('mbmAutofillBtn');
  if (mbmAutofillBtn) {
    mbmAutofillBtn.addEventListener('click', () => autofillSquad('mbmPlayerInput', 'mbmFormatSelect'));
  }

  renderFacilityTabs();
  renderMeta();
  renderDateTabs();
  renderSlots();
  renderMyBookings();
}

window.openTeamBookingModal = openTeamBookingModal;
window.closeTeamBookingModal = closeTeamBookingModal;
window.openModifyBookingModal = openModifyBookingModal;
window.closeModifyBookingModal = closeModifyBookingModal;
window.switchModifyTab = switchModifyTab;
window.autofillSquad = autofillSquad;
window.updateRequirementBadge = updateRequirementBadge;
window.updateModifyReqBadge = updateModifyReqBadge;

function getFacilityIcon(courtId) {
  if (courtId === 'CRT1') return '🏸';
  if (courtId === 'CRT2') return '🎾';
  if (courtId === 'CRT3') return '🏀';
  if (courtId === 'CRT4') return '⚽';
  if (courtId === 'CRT5') return '🏏';
  return '🏟️';
}

function openAdminBookingModal() {
  const modal = document.getElementById('adminBookingModal');
  if (modal) {
    const err = document.getElementById('abmErrorAlert');
    if (err) err.style.display = 'none';
    modal.classList.remove('hidden');
  }
}

function closeAdminBookingModal() {
  const modal = document.getElementById('adminBookingModal');
  if (modal) modal.classList.add('hidden');
}

window.openAdminBookingModal = openAdminBookingModal;
window.closeAdminBookingModal = closeAdminBookingModal;

async function initFacilitiesAdmin() {
  const tbody = document.getElementById('allReservationsBody');
  if (!tbody) return; // Not on admin page

  const toastEl = document.getElementById('bookingToast');
  function showToast(msg) {
    if (!toastEl) return;
    toastEl.textContent = msg;
    toastEl.classList.add('show');
    clearTimeout(showToast._t);
    showToast._t = setTimeout(() => toastEl.classList.remove('show'), 2800);
  }

  async function fetchAndRenderAdminBookings() {
    tbody.innerHTML = `<tr><td colspan="5" style="color:var(--ink-soft); text-align:center; padding:16px;">Syncing reservations from Java server…</td></tr>`;

    let reservations = [];

    // 1. Fetch live server data
    try {
      const res = await fetch('/api/data');
      if (res.ok) {
        const data = await res.json();
        if (data && data.courts) {
          data.courts.forEach(court => {
            const cId = court.courtId;
            const cType = court.courtType;
            if (court.reservations) {
              Object.entries(court.reservations).forEach(([slot, info]) => {
                reservations.push({
                  courtId: cId,
                  courtName: `${getFacilityIcon(cId)} ${cType} (${cId})`,
                  date: 'Today',
                  slot: slot,
                  userId: info.userId || 'Student',
                  userName: info.name || 'Reserved',
                  format: info.format || 'Standard',
                  players: info.players || []
                });
              });
            }
          });
        }
      }
    } catch(e) {}

    // 2. Merge local demo bookings if not already present
    const localBookings = loadBookings();
    Object.values(localBookings).forEach(b => {
      const cId = COURT_MAPPING[b.facility] || 'CRT1';
      const f = FACILITIES.find(x => x.id === b.facility) || { name: b.facility, ic: '🏟️' };
      const slotStr = b.slotStr || `${b.time}-${String(parseInt(b.time, 10)+1).padStart(2, '0')}:00`;
      if (!reservations.some(r => r.courtId === cId && r.slot === slotStr)) {
        reservations.push({
          courtId: cId,
          courtName: `${f.ic} ${f.name} (${cId})`,
          date: b.date || 'Today',
          slot: slotStr,
          userId: b.userId || 'S101',
          userName: b.userName || 'Rahul Sharma',
          format: b.format || 'Standard',
          players: b.players || []
        });
      }
    });

    if (reservations.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="5" style="text-align:center; color:var(--ink-soft); padding:20px;">
            No active reservations at the moment. All courts and turf facilities are currently free.
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = reservations.map(r => {
      const count = r.players ? r.players.length : 1;
      const badge = r.format ? `<span class="badge blue" style="font-size:.7rem; margin-left:6px;">${r.format} (${count}p)</span>` : '';
      return `<tr>
        <td><strong>${r.courtName}</strong></td>
        <td>${r.date}</td>
        <td><strong>${r.slot}</strong></td>
        <td>${r.userName} (${r.userId}) ${badge}</td>
        <td>
          <button type="button" class="btn btn-outline btn-sm" style="color:var(--red); border-color:#fca5a5;"
                  data-admin-release-court="${r.courtId}" data-admin-release-slot="${r.slot}">
            Cancel (Override)
          </button>
        </td>
      </tr>`;
    }).join('');

    tbody.querySelectorAll('[data-admin-release-court]').forEach(btn => {
      btn.addEventListener('click', async () => {
        const cId = btn.getAttribute('data-admin-release-court');
        const slot = btn.getAttribute('data-admin-release-slot');

        try {
          await fetch('/api/release', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ courtId: cId, slot: slot, userId: 'admin' })
          });
        } catch(e) {}

        const bks = loadBookings();
        Object.keys(bks).forEach(k => {
          if ((COURT_MAPPING[bks[k].facility] === cId || bks[k].courtId === cId) &&
              (bks[k].slotStr === slot || bks[k].time === slot.split('-')[0])) {
            delete bks[k];
          }
        });
        saveBookings(bks);

        showToast(`Slot '${slot}' on ${cId} released by Admin Override.`);
        fetchAndRenderAdminBookings();
      });
    });
  }

  // Refresh button
  const refreshBtn = document.getElementById('refreshAdminBookingsBtn');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', fetchAndRenderAdminBookings);
  }

  // Admin Book Court button
  const bookBtn = document.getElementById('adminBookCourtBtn');
  if (bookBtn) {
    bookBtn.addEventListener('click', openAdminBookingModal);
  }

  // Autofill button in admin modal
  const abmAutofillBtn = document.getElementById('abmAutofillBtn');
  if (abmAutofillBtn) {
    abmAutofillBtn.addEventListener('click', () => {
      const fmt = document.getElementById('abmFormatSelect').value;
      let count = 2;
      if (fmt.includes('22') || fmt.includes('11')) count = 22;
      else if (fmt.includes('14') || fmt.includes('7')) count = 14;
      else if (fmt.includes('4')) count = 4;
      else if (fmt.includes('10')) count = 10;
      else if (fmt.includes('6')) count = 6;

      const squad = ['F201 (Coach Vikram)'];
      for (let i = 2; i <= count; i++) squad.push(`S10${i > 9 ? i : '0' + i}`);
      document.getElementById('abmPlayerInput').value = squad.join(', ');
    });
  }

  // Confirm booking in admin modal
  const abmConfirmBtn = document.getElementById('abmConfirmBtn');
  if (abmConfirmBtn) {
    abmConfirmBtn.addEventListener('click', async () => {
      const courtId = document.getElementById('abmCourtSelect').value;
      const slot = document.getElementById('abmSlotSelect').value;
      const format = document.getElementById('abmFormatSelect').value;
      const rawPlayers = document.getElementById('abmPlayerInput').value;
      const players = rawPlayers.split(',').map(s => s.trim()).filter(s => s.length > 0);
      const errEl = document.getElementById('abmErrorAlert');

      try {
        const res = await fetch('/api/reserve', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            courtId: courtId,
            slot: slot,
            userId: 'F201',
            format: format,
            players: players.length > 0 ? players.join(',') : 'F201,Player_2'
          })
        });
        const data = await res.json();
        if (!data.success && data.error) {
          errEl.textContent = data.error;
          errEl.style.display = 'block';
          return;
        }
      } catch(e) {}

      closeAdminBookingModal();
      showToast(`Reserved ${slot} on ${courtId} (Admin Override).`);
      fetchAndRenderAdminBookings();
    });
  }

  fetchAndRenderAdminBookings();
}

document.addEventListener('DOMContentLoaded', () => {
  initSportsBooking();
  initFacilitiesAdmin();
});


