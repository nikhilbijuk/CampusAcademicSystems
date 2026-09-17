// Sports & Facilities booking — courts/turf slot reservation.
// Demo persistence uses localStorage (per-browser). Swap `saveBooking` /
// `loadBookings` for calls to a real /api/bookings endpoint once the Java
// backend has a matching servlet + `bookings` table.

const FACILITIES = [
  { id: 'bball',     name: 'Basketball Court A', ic: '🏀', location: 'North Sports Block', type: 'Outdoor · hard court' },
  { id: 'turf',      name: 'Football Turf',       ic: '⚽', location: 'Main Ground',        type: 'Outdoor · synthetic turf' },
  { id: 'tennis',    name: 'Tennis Court',        ic: '🎾', location: 'East Courts',        type: 'Outdoor · clay court' },
  { id: 'badminton', name: 'Badminton Court',      ic: '🏸', location: 'Indoor Sports Hall',  type: 'Indoor · wooden court, AC' },
];

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

// Deterministic "other students already booked this" seed, so the grid
// looks like a real campus facility instead of empty every time.
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

  if (isToday && parseInt(time, 10) <= currentHour) return 'past';

  const hash = seedHash(key);
  if (hash % 5 === 0) return 'booked-other'; // ~20% pre-occupied by other students
  return 'free';
}

/* ----------------------------------------------------------------
   Student-side rendering: facility tabs, date tabs, slot grid.
   Expects containers with ids: facilityTabs, facilityMeta, dateTabs, slotGrid, myBookingsList
------------------------------------------------------------------*/
function initSportsBooking() {
  const facilityTabsEl = document.getElementById('facilityTabs');
  if (!facilityTabsEl) return; // not on this page

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
      const disabled = state === 'booked-other' || state === 'past';
      return `<button type="button" class="slot-btn ${state}" data-time="${time}" ${disabled ? 'disabled' : ''}>
        ${time}–${endHour}:00
      </button>`;
    }).join('');

    slotGridEl.querySelectorAll('.slot-btn').forEach(btn => {
      btn.addEventListener('click', () => toggleSlot(btn.getAttribute('data-time')));
    });
  }

  async function toggleSlot(time) {
    const bookings = loadBookings();
    const key = slotKey(activeFacility, activeDate, time);
    const courtMapping = {
      'badminton': 'CRT1',
      'tennis': 'CRT2',
      'bball': 'CRT3',
      'turf': 'CRT1'
    };
    const courtId = courtMapping[activeFacility] || 'CRT1';
    const timeSlotStr = `${time}-${String(parseInt(time, 10)+1).padStart(2, '0')}:00`;

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

    if (bookings[key] && bookings[key].by === 'you') {
      try {
        await fetch('/api/release', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ courtId: courtId, slotTime: timeSlotStr, userId: userId })
        });
      } catch(e) {}
      delete bookings[key];
      showToast('Reservation cancelled.');
    } else {
      try {
        const res = await fetch('/api/reserve', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ courtId: courtId, slotTime: timeSlotStr, userId: userId })
        });
        const data = await res.json();
        if (!data.success && data.error) {
          showToast(data.error);
          return;
        }
      } catch(e) {}
      bookings[key] = { by: 'you', facility: activeFacility, date: activeDate, time, userId, userName };
      const f = FACILITIES.find(x => x.id === activeFacility);
      showToast(`${f.name} booked for ${time} on ${activeDate}.`);
    }
    saveBookings(bookings);
    renderSlots();
    renderMyBookings();
  }

  function renderMyBookings() {
    if (!myBookingsEl) return;
    const bookings = loadBookings();
    const mine = Object.values(bookings).filter(b => b.by === 'you')
      .sort((a, b) => (a.date + a.time).localeCompare(b.date + b.time));

    if (mine.length === 0) {
      myBookingsEl.innerHTML = `<p style="font-size:.85rem; color:var(--ink-soft);">You haven't reserved any slots yet — pick a facility and time above.</p>`;
      return;
    }
    myBookingsEl.innerHTML = mine.map(b => {
      const f = FACILITIES.find(x => x.id === b.facility) || { name: b.facility, ic: '🏟️' };
      const endHour = String(parseInt(b.time, 10) + 1).padStart(2, '0');
      return `<div class="notif-item">
        <div class="notif-dot"></div>
        <div><p><strong>${f.ic} ${f.name}</strong> — ${b.date}, ${b.time}–${endHour}:00</p></div>
        <a href="#" class="cancel-link" data-cancel-facility="${b.facility}" data-cancel-date="${b.date}" data-cancel-time="${b.time}">Cancel</a>
      </div>`;
    }).join('');

    myBookingsEl.querySelectorAll('[data-cancel-facility]').forEach(link => {
      link.addEventListener('click', async (e) => {
        e.preventDefault();
        const fId = link.getAttribute('data-cancel-facility');
        const dStr = link.getAttribute('data-cancel-date');
        const tStr = link.getAttribute('data-cancel-time');
        const bk = loadBookings();
        delete bk[slotKey(fId, dStr, tStr)];
        saveBookings(bk);

        try {
          const courtMapping = { 'badminton': 'CRT1', 'tennis': 'CRT2', 'bball': 'CRT3', 'turf': 'CRT1' };
          const timeSlotStr = `${tStr}-${String(parseInt(tStr, 10)+1).padStart(2, '0')}:00`;
          await fetch('/api/release', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ courtId: courtMapping[fId] || 'CRT1', slotTime: timeSlotStr, userId: 'S101' })
          });
        } catch(e) {}

        renderMyBookings();
        renderSlots();
        showToast('Reservation cancelled.');
      });
    });
  }

  function showToast(msg) {
    if (!toastEl) return;
    toastEl.textContent = msg;
    toastEl.classList.add('show');
    clearTimeout(showToast._t);
    showToast._t = setTimeout(() => toastEl.classList.remove('show'), 2600);
  }

  renderFacilityTabs();
  renderMeta();
  renderDateTabs();
  renderSlots();
  renderMyBookings();
}

/* ----------------------------------------------------------------
   Admin-side rendering: all reservations across every student.
   Expects a container with id="allReservationsBody" (a <tbody>)
------------------------------------------------------------------*/
function initFacilitiesAdmin() {
  const tbody = document.getElementById('allReservationsBody');
  if (!tbody) return;

  function render() {
    const bookings = loadBookings();
    const entries = Object.entries(bookings);

    if (entries.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td>🏀 Basketball Court A</td>
          <td>2026-09-18</td>
          <td>17:00–18:00</td>
          <td>Priya Nair (S102)</td>
          <td><span class="badge green">Active</span></td>
        </tr>
        <tr>
          <td>🏸 Badminton Court</td>
          <td>2026-09-18</td>
          <td>18:00–19:00</td>
          <td>Rahul Sharma (S101)</td>
          <td><span class="badge green">Active</span></td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = entries
      .sort((a, b) => (a[1].date + a[1].time).localeCompare(b[1].date + b[1].time))
      .map(([key, b]) => {
        const f = FACILITIES.find(x => x.id === b.facility) || { name: b.facility, ic: '' };
        const endHour = String(parseInt(b.time, 10) + 1).padStart(2, '0');
        const studentDesc = b.userName ? `${b.userName} (${b.userId || 'Student'})` : 'Student (S101)';
        return `<tr>
          <td>${f.ic} ${f.name}</td>
          <td>${b.date}</td>
          <td>${b.time}–${endHour}:00</td>
          <td>${studentDesc}</td>
          <td><a href="#" class="btn btn-ghost btn-sm" data-cancel-key="${key}">Cancel</a></td>
        </tr>`;
      }).join('');

    tbody.querySelectorAll('[data-cancel-key]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        const bk = loadBookings();
        delete bk[btn.getAttribute('data-cancel-key')];
        saveBookings(bk);
        render();
      });
    });
  }

  const refreshBtn = document.getElementById('refreshAdminBookingsBtn');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', render);
  }

  render();
}

document.addEventListener('DOMContentLoaded', () => {
  initSportsBooking();
  initFacilitiesAdmin();
});
