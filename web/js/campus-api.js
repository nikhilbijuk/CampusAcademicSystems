/**
 * Campus Academic Systems — API Bridge & Interactive Dynamic Wiring
 * Connects Teammate Frontend Dashboards to Embedded Java Web Server (/api/*)
 * and provides smooth offline/demo fallbacks.
 */

(function() {
  const API = {
    DATA: '/api/data',
    RESERVE: '/api/reserve',
    RELEASE: '/api/release',
    HOSTEL_LEAVE: '/api/hostel/leave',
    HOSTEL_BILL: '/api/hostel/bill',
    LIBRARY_BORROW: '/api/library/borrow',
    LIBRARY_RETURN: '/api/library/return',
    FINE: '/api/fine'
  };

  let isServerLive = false;
  let campusData = null;

  async function checkServerStatus() {
    try {
      const res = await fetch(API.DATA);
      if (res.ok) {
        isServerLive = true;
        campusData = await res.json();
        updateConnectionBadge(true);
        onServerDataLoaded(campusData);
      } else {
        updateConnectionBadge(false);
      }
    } catch (e) {
      updateConnectionBadge(false);
    }
  }

  function updateConnectionBadge(live) {
    const badges = document.querySelectorAll('#connectionStatusBadge');
    badges.forEach(b => {
      if (live) {
        b.textContent = '● Java Server Online';
        b.className = 'badge green';
      } else {
        b.textContent = '● Standalone Mode';
        b.className = 'badge amber';
      }
    });
  }

  function showToast(msg) {
    const toast = document.getElementById('bookingToast');
    if (!toast) {
      alert(msg);
      return;
    }
    toast.textContent = msg;
    toast.classList.add('show');
    clearTimeout(showToast._t);
    showToast._t = setTimeout(() => toast.classList.remove('show'), 2800);
  }

  function initHostelSection() {
    const applyBtn = document.getElementById('applyLeaveBtn');
    const refreshBillBtn = document.getElementById('refreshBillBtn');
    const leaveInput = document.getElementById('leaveDaysInput');
    const rollEl = document.getElementById('hRollNo');

    if (applyBtn) {
      applyBtn.addEventListener('click', async () => {
        const days = parseInt(leaveInput.value, 10);
        if (isNaN(days) || days <= 0) {
          showToast('Please enter a valid number of days (1–15).');
          return;
        }

        const rollNo = rollEl ? rollEl.textContent.trim() : 'STU202';

        if (isServerLive) {
          try {
            const res = await fetch(API.HOSTEL_LEAVE, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ rollNo: rollNo, days: days })
            });
            const data = await res.json();
            if (data.success) {
              showToast(`Mess leave approved for ${days} days!`);
              if (document.getElementById('hLeavesRecorded')) {
                document.getElementById('hLeavesRecorded').textContent = `${data.leavesThisMonth} days this month`;
              }
              fetchAndRenderBill(rollNo);
            } else {
              showToast(data.error || 'Failed to submit leave.');
            }
          } catch (err) {
            showToast('Network error while recording leave.');
          }
        } else {
          // Offline fallback
          const curLeaves = parseInt(sessionStorage.getItem('demo_leaves') || '0', 10) + days;
          sessionStorage.setItem('demo_leaves', curLeaves);
          if (document.getElementById('hLeavesRecorded')) {
            document.getElementById('hLeavesRecorded').textContent = `${curLeaves} days this month`;
          }
          const baseRoom = 5400;
          const messRate = 150;
          const messCost = Math.max(0, (30 - curLeaves) * messRate);
          const total = baseRoom + messCost;
          updateBillUI(total, baseRoom, messCost, curLeaves);
          showToast(`Mess leave of ${days} days applied (Rebate: Rs. ${days * 150}).`);
        }
        leaveInput.value = '';
      });
    }

    if (refreshBillBtn) {
      refreshBillBtn.addEventListener('click', () => {
        const rollNo = rollEl ? rollEl.textContent.trim() : 'STU202';
        fetchAndRenderBill(rollNo);
      });
    }
  }

  async function fetchAndRenderBill(rollNo) {
    if (isServerLive) {
      try {
        const res = await fetch(`${API.HOSTEL_BILL}?rollNo=${encodeURIComponent(rollNo)}&monthDays=30`);
        if (res.ok) {
          const bill = await res.json();
          updateBillUI(bill.totalDue, bill.roomCost, bill.messCost, bill.leavesThisMonth);
          showToast('Itemized mess bill updated from server.');
          return;
        }
      } catch (e) {
        console.warn('Error fetching bill from server', e);
      }
    }

    // Offline fallback
    const curLeaves = parseInt(sessionStorage.getItem('demo_leaves') || '0', 10);
    const baseRoom = 5400;
    const messRate = 150;
    const messCost = Math.max(0, (30 - curLeaves) * messRate);
    const total = baseRoom + messCost;
    updateBillUI(total, baseRoom, messCost, curLeaves);
  }

  function updateBillUI(total, roomCost, messCost, leaves) {
    const totalEl = document.getElementById('billTotalDue');
    const noteEl = document.getElementById('billRebateNote');
    const breakdownEl = document.getElementById('billBreakdownText');

    if (totalEl) totalEl.textContent = `Rs. ${Number(total).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
    if (noteEl) noteEl.textContent = `Room: Rs. ${roomCost} | Mess: Rs. ${messCost}`;
    if (breakdownEl) {
      breakdownEl.innerHTML = `
        <strong>Itemized Monthly Billing Breakdown:</strong><br>
        • Room Tariff (Single Occupancy): Rs. ${Number(roomCost).toFixed(2)}<br>
        • Base Mess Tariff (30 days @ Rs. 150/day): Rs. 4,500.00<br>
        • Leave Rebate Deducted: <span style="color:var(--teal); font-weight:600;">- Rs. ${(leaves * 150).toFixed(2)} (${leaves} days approved)</span><br>
        • Net Mess Payable: Rs. ${Number(messCost).toFixed(2)}<br>
        • <strong>Total Due: Rs. ${Number(total).toFixed(2)}</strong>
      `;
    }
  }

  function initLibrarySection() {
    document.querySelectorAll('.book-borrow-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        const isbn = btn.getAttribute('data-isbn');
        const userProfileRaw = sessionStorage.getItem('ridgeview_profile');
        let userId = 'S101';
        if (userProfileRaw) {
          try { userId = JSON.parse(userProfileRaw).id || 'S101'; } catch(e){}
        }

        if (isServerLive) {
          try {
            const res = await fetch(API.LIBRARY_BORROW, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ isbn: isbn, userId: userId })
            });
            const data = await res.json();
            if (data.success) {
              showToast(data.message || 'Book issued successfully!');
              btn.textContent = 'Issued';
              btn.disabled = true;
              const countEl = document.getElementById('libIssuedCount');
              if (countEl) countEl.textContent = String(parseInt(countEl.textContent || '0', 10) + 1);
            } else {
              showToast(data.error || 'Cannot borrow book.');
            }
          } catch (e) {
            showToast('Network error while issuing book.');
          }
        } else {
          // Offline fallback
          btn.textContent = 'Issued';
          btn.disabled = true;
          const countEl = document.getElementById('libIssuedCount');
          if (countEl) countEl.textContent = String(parseInt(countEl.textContent || '0', 10) + 1);
          showToast(`Book issued to student ID ${userId}. Return within 14 days.`);
        }
      });
    });
  }

  function initFinesSection() {
    const payFineBtn = document.getElementById('payFineBtn');
    const payFineInput = document.getElementById('payFineAmount');

    if (payFineBtn) {
      payFineBtn.addEventListener('click', async () => {
        const amt = parseFloat(payFineInput.value);
        if (isNaN(amt) || amt <= 0) {
          showToast('Please enter a valid amount.');
          return;
        }

        const userProfileRaw = sessionStorage.getItem('ridgeview_profile');
        let userId = 'S101';
        if (userProfileRaw) {
          try { userId = JSON.parse(userProfileRaw).id || 'S101'; } catch(e){}
        }

        if (isServerLive) {
          try {
            const res = await fetch(API.FINE, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ userId: userId, action: 'pay', amount: amt })
            });
            const data = await res.json();
            if (data.success) {
              showToast(`Payment of Rs. ${amt.toFixed(2)} received successfully!`);
              updateFineBalanceUI(data.fineBalance);
            } else {
              showToast(data.error || 'Payment failed.');
            }
          } catch(e) {
            showToast('Network error processing payment.');
          }
        } else {
          showToast(`Payment of Rs. ${amt.toFixed(2)} processed!`);
          updateFineBalanceUI(0);
        }
      });
    }

    const adminAddBtn = document.getElementById('adminAddFineBtn');
    const adminPayBtn = document.getElementById('adminPayFineBtn');
    const adminUserInput = document.getElementById('adminFineUserId');
    const adminAmtInput = document.getElementById('adminFineAmount');

    const handleAdminFine = async (action) => {
      const uid = adminUserInput ? adminUserInput.value.trim() : '';
      const amt = parseFloat(adminAmtInput ? adminAmtInput.value : '0');
      if (!uid || isNaN(amt) || amt <= 0) {
        showToast('Please specify target User ID and valid amount.');
        return;
      }

      if (isServerLive) {
        try {
          const res = await fetch(API.FINE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: uid, action: action, amount: amt })
          });
          const data = await res.json();
          if (data.success) {
            showToast(`User ${uid}: fine balance updated to Rs. ${data.fineBalance.toFixed(2)}.`);
            checkServerStatus();
          } else {
            showToast(data.error || 'Operation failed.');
          }
        } catch(e) {
          showToast('Error communicating with server.');
        }
      } else {
        showToast(`Offline demo: User ${uid} fine ${action === 'add' ? 'assessed' : 'waived'} (Rs. ${amt}).`);
      }
    };

    if (adminAddBtn) adminAddBtn.addEventListener('click', () => handleAdminFine('add'));
    if (adminPayBtn) adminPayBtn.addEventListener('click', () => handleAdminFine('pay'));
  }

  function updateFineBalanceUI(bal) {
    const fineEl = document.getElementById('pFineStatus');
    const libFineEl = document.getElementById('libFineBalance');
    const finesTotalDue = document.getElementById('finesTotalDue');
    const finesDeltaMsg = document.getElementById('finesDeltaMsg');
    const finesBadge = document.getElementById('finesAccountBadge');

    if (bal > 0) {
      if (fineEl) fineEl.innerHTML = `<span class="badge red">Overdue Fine: Rs. ${bal.toFixed(2)}</span>`;
      if (libFineEl) libFineEl.textContent = `Rs. ${bal.toFixed(2)}`;
      if (finesTotalDue) finesTotalDue.textContent = `Rs. ${bal.toFixed(2)}`;
      if (finesDeltaMsg) finesDeltaMsg.textContent = 'Account hold active: facility booking restricted';
      if (finesBadge) { finesBadge.textContent = 'Action Required'; finesBadge.className = 'badge red'; }
    } else {
      if (fineEl) fineEl.innerHTML = `<span class="badge green">Zero Dues (Rs. 0.00)</span>`;
      if (libFineEl) libFineEl.textContent = `Rs. 0.00`;
      if (finesTotalDue) finesTotalDue.textContent = `Rs. 0.00`;
      if (finesDeltaMsg) finesDeltaMsg.textContent = 'Zero outstanding overdue penalties';
      if (finesBadge) { finesBadge.textContent = 'Good Standing'; finesBadge.className = 'badge green'; }
    }
  }

  function onServerDataLoaded(data) {
    if (!data) return;

    // Update user profile fine balance if available
    const userProfileRaw = sessionStorage.getItem('ridgeview_profile');
    if (userProfileRaw && data.users) {
      try {
        const prof = JSON.parse(userProfileRaw);
        const serverUser = data.users.find(u => u.userId.toUpperCase() === prof.id.toUpperCase());
        if (serverUser) {
          updateFineBalanceUI(serverUser.fineBalance || 0);
        }
      } catch (e) {}
    }

    // Populate admin table if on admin dashboard
    const adminHostelTable = document.getElementById('adminHostelStudentsTable');
    if (adminHostelTable && data.hostelStudents && data.hostelStudents.length > 0) {
      adminHostelTable.innerHTML = data.hostelStudents.map(hs => `
        <tr>
          <td><strong>${hs.rollNo}</strong></td>
          <td>${hs.name}</td>
          <td>Room ${hs.roomNumber} (${hs.roomType})</td>
          <td>Rs. ${Number(hs.roomTariff).toFixed(2)}</td>
          <td>${hs.mealPlan}</td>
          <td>${hs.leavesThisMonth} days</td>
          <td><span class="badge green">Active Resident</span></td>
        </tr>
      `).join('');
    }

    const adminFinesTable = document.getElementById('adminFinesTable');
    if (adminFinesTable && data.users && data.users.length > 0) {
      adminFinesTable.innerHTML = data.users.map(u => `
        <tr>
          <td><strong>${u.userId}</strong></td>
          <td>${u.name}</td>
          <td>${u.role}</td>
          <td>Rs. ${Number(u.fineBalance).toFixed(2)}</td>
          <td><span class="badge ${u.fineBalance > 0 ? 'red' : 'green'}">${u.fineBalance > 0 ? 'Hold' : 'Clear'}</span></td>
          <td><button class="btn btn-ghost btn-sm" onclick="document.getElementById('adminFineUserId').value='${u.userId}'">Select</button></td>
        </tr>
      `).join('');
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    checkServerStatus();
    initHostelSection();
    initLibrarySection();
    initFinesSection();
  });
})();
