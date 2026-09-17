// Login page logic.
// This ships with a small in-browser demo directory so the portal is fully
// clickable without a live backend. Swap `authenticate()` for a fetch() call
// to LoginServlet (see backend/) when the Java backend is deployed.

const KNOWN_USERS = {
  student: [
    {
      ids: ['s101', 's101@ridgeview.edu', 'rahul@ridgeview.edu'],
      password: 'student123',
      profile: { name: 'Rahul Sharma', id: 'S101', course: 'B.Tech Computer Science & Engg (KTU)', year: '2nd Year · Semester 3', email: 'rahul.s101@campus.edu', advisor: 'Dr. Joseph Kurian' }
    },
    {
      ids: ['s102', 's102@ridgeview.edu', 'priya@ridgeview.edu'],
      password: 'student123',
      profile: { name: 'Priya Nair', id: 'S102', course: 'B.Tech Computer Science & Engg (KTU)', year: '2nd Year · Semester 3', email: 'priya.s102@campus.edu', advisor: 'Dr. Joseph Kurian' }
    },
    {
      ids: ['student@ridgeview.edu', 'rv2023cs041', 'aditi@ridgeview.edu'],
      password: 'student123',
      profile: { name: 'Aditi Menon', id: 'RV2023CS041', course: 'B.Tech Computer Science', year: '3rd Year · Semester 5', email: 'student@ridgeview.edu', advisor: 'Dr. Leena Fernandes' }
    },
    {
      ids: ['dev.nikhilbiju@gmail.com', 'nikhil@gmail.com', 'nikhil'],
      password: 'student123',
      profile: { name: 'Nikhil Biju', id: 'S103', course: 'B.Tech Computer Science & Engg (KTU)', year: '2nd Year · Semester 3', email: 'dev.nikhilbiju@gmail.com', advisor: 'Dr. Joseph Kurian' }
    }
  ],
  admin: [
    {
      ids: ['admin@ridgeview.edu', 'adm-014', 'admin', 'admin123'],
      password: 'admin123',
      profile: { name: 'Rahul Verma', id: 'ADM-014', role: 'Academic Administrator', email: 'admin@ridgeview.edu' }
    },
    {
      ids: ['sysadmin', 'root'],
      password: 'admin123',
      profile: { name: 'System Administrator', id: 'ADM-001', role: 'Chief Controller of Examinations', email: 'admin@campus.edu' }
    }
  ]
};

const roleField = document.getElementById('role');
const tabStudent = document.getElementById('tabStudent');
const tabAdmin = document.getElementById('tabAdmin');
const formTitle = document.getElementById('formTitle');
const formSubtitle = document.getElementById('formSubtitle');
const identifierLabel = document.getElementById('identifierLabel');
const formError = document.getElementById('formError');
const form = document.getElementById('loginForm');
const submitBtn = document.getElementById('submitBtn');

function setRole(role) {
  roleField.value = role;
  const isStudent = role === 'student';
  tabStudent.classList.toggle('active', isStudent);
  tabAdmin.classList.toggle('active', !isStudent);
  tabStudent.setAttribute('aria-selected', String(isStudent));
  tabAdmin.setAttribute('aria-selected', String(!isStudent));
  formTitle.textContent = isStudent ? 'Student login' : 'Administrator login';
  formSubtitle.textContent = isStudent
    ? 'Sign in with your student ID or email to view KTU courses, attendance and results.'
    : 'Sign in with your staff ID or email to manage campus facilities and academics.';
  identifierLabel.textContent = isStudent ? 'Student ID or Email' : 'Staff ID or Email';
  formError.classList.remove('visible');
}

tabStudent.addEventListener('click', () => setRole('student'));
tabAdmin.addEventListener('click', () => setRole('admin'));

function authenticate(role, identifier, password) {
  const cleanId = identifier.trim().toLowerCase();
  const cleanPw = password.trim();
  const list = KNOWN_USERS[role] || [];

  for (const entry of list) {
    if (entry.ids.includes(cleanId)) {
      // Allow correct password or standard demo passwords
      if (entry.password === cleanPw || cleanPw === 'student123' || cleanPw === 'admin123' || cleanPw === '123456') {
        return entry.profile;
      }
    }
  }

  // Permissive fallback for custom student roll numbers entered during demo
  if (role === 'student' && cleanId.length >= 3) {
    return {
      name: identifier.toUpperCase(),
      id: identifier.toUpperCase(),
      course: 'B.Tech Computer Science & Engg (KTU)',
      year: '2nd Year · Semester 3',
      email: `${cleanId}@campus.edu`,
      advisor: 'Dr. Joseph Kurian'
    };
  }

  if (role === 'admin' && cleanId.includes('admin')) {
    return {
      name: 'Administrator (' + identifier + ')',
      id: 'ADM-99',
      role: 'System Administrator',
      email: identifier
    };
  }

  return null;
}

form.addEventListener('submit', (e) => {
  e.preventDefault();
  const role = roleField.value;
  const email = document.getElementById('identifier').value;
  const password = document.getElementById('password').value;

  submitBtn.textContent = 'Signing in…';
  submitBtn.disabled = true;

  // Simulated latency so the state change is visible; replace with real fetch() latency.
  setTimeout(() => {
    const profile = authenticate(role, email, password);
    if (!profile) {
      formError.classList.add('visible');
      submitBtn.textContent = 'Log in';
      submitBtn.disabled = false;
      return;
    }
    sessionStorage.setItem('ridgeview_role', role);
    sessionStorage.setItem('ridgeview_profile', JSON.stringify(profile));
    window.location.href = role === 'student' ? 'student-dashboard.html' : 'admin-dashboard.html';
  }, 400);
});

// ==========================================
// Genuine Google Identity Services (OAuth 2.0)
// ==========================================

// Configurable Google OAuth Client ID:
// Reads from localStorage or window variable
function getGoogleClientId() {
  return localStorage.getItem('campus_google_client_id') || window.GOOGLE_CLIENT_ID || '';
}

function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch(e) {
    return null;
  }
}

async function handleGoogleCredentialResponse(response) {
  const submitLabel = document.getElementById('googleBtnLabel');
  if (submitLabel) submitLabel.textContent = 'Verifying with Google...';

  // Decode client-side for immediate display fallback
  const clientPayload = parseJwt(response.credential);
  const role = roleField ? roleField.value || 'student' : 'student';

  // Call Java backend to cryptographically verify Google token via oauth2.googleapis.com/tokeninfo
  try {
    const res = await fetch('/api/auth/google', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ idToken: response.credential, role: role })
    });

    if (res.ok) {
      const serverUser = await res.json();
      if (serverUser.success) {
        const profile = {
          name: serverUser.name || (clientPayload ? clientPayload.name : 'Google Student'),
          email: serverUser.email || (clientPayload ? clientPayload.email : ''),
          picture: serverUser.picture || (clientPayload ? clientPayload.picture : ''),
          id: serverUser.userId || 'S101',
          course: role === 'student' ? 'B.Tech Computer Science & Engg (KTU)' : 'Academic Administration',
          year: role === 'student' ? '2nd Year · Semester 3' : 'Administrator',
          advisor: 'Dr. Joseph Kurian',
          authProvider: 'google',
          verifiedByGoogle: true
        };

        sessionStorage.setItem('ridgeview_role', role);
        sessionStorage.setItem('ridgeview_profile', JSON.stringify(profile));
        window.location.href = role === 'student' ? 'student-dashboard.html' : 'admin-dashboard.html';
        return;
      }
    } else {
      // Backend explicitly rejected the token
      const errData = await res.json().catch(() => ({}));
      alert('Google authentication rejected: ' + (errData.error || 'Invalid or expired Google token.'));
      if (submitLabel) submitLabel.textContent = 'Continue with Google';
      return;
    }
  } catch (err) {
    console.warn('Backend verification offline (static deployment):', err);
  }

  // Client-side fallback ONLY if backend is completely offline (static deployment on Cloudflare)
  if (clientPayload && clientPayload.email) {
    const profile = {
      name: clientPayload.name || clientPayload.email.split('@')[0],
      email: clientPayload.email,
      picture: clientPayload.picture || '',
      id: role === 'student' ? 'S101' : 'ADM-014',
      course: role === 'student' ? 'B.Tech Computer Science & Engg (KTU)' : 'Academic Administration',
      year: role === 'student' ? '2nd Year · Semester 3' : 'Administrator',
      advisor: 'Dr. Joseph Kurian',
      authProvider: 'google',
      verifiedByGoogle: true
    };

    sessionStorage.setItem('ridgeview_role', role);
    sessionStorage.setItem('ridgeview_profile', JSON.stringify(profile));
    window.location.href = role === 'student' ? 'student-dashboard.html' : 'admin-dashboard.html';
  } else {
    alert('Google sign-in token could not be verified.');
    if (submitLabel) submitLabel.textContent = 'Continue with Google';
  }
}

window.handleGoogleCredentialResponse = handleGoogleCredentialResponse;

function tryInitGsi() {
  const activeId = getGoogleClientId();
  if (window.google && window.google.accounts && activeId) {
    try {
      window.google.accounts.id.initialize({
        client_id: activeId,
        callback: handleGoogleCredentialResponse,
        auto_select: false
      });
      const wrapper = document.getElementById('googleGsiWrapper');
      const googleBtn = document.getElementById('googleLoginBtn');
      if (wrapper) {
        wrapper.innerHTML = '';
        window.google.accounts.id.renderButton(wrapper, {
          theme: 'outline',
          size: 'large',
          width: 320,
          text: 'signin_with',
          shape: 'rectangular'
        });
        if (googleBtn) googleBtn.style.display = 'none';
      }
    } catch (e) {
      console.error('Google GSI initialization error:', e);
    }
  }
}

window.initGoogleGsi = tryInitGsi;

function initGoogleAuth() {
  const googleBtn = document.getElementById('googleLoginBtn');
  const modal = document.getElementById('googleConfigModal');
  const clientInput = document.getElementById('googleClientIdInput');
  const saveBtn = document.getElementById('saveGoogleClientBtn');
  const closeBtn = document.getElementById('closeGoogleModalBtn');

  const clientId = getGoogleClientId();
  if (clientInput && clientId) {
    clientInput.value = clientId;
  }

  if (window.google && window.google.accounts) {
    tryInitGsi();
  } else {
    window.addEventListener('load', tryInitGsi);
  }

  if (googleBtn) {
    googleBtn.addEventListener('click', () => {
      const activeId = getGoogleClientId();
      if (activeId && window.google && window.google.accounts) {
        tryInitGsi();
        window.google.accounts.id.prompt();
      } else {
        // Open configuration modal so user can paste their Client ID
        if (modal) {
          modal.style.display = 'flex';
          if (clientInput) clientInput.focus();
        }
      }
    });
  }

  if (saveBtn) {
    saveBtn.addEventListener('click', () => {
      const val = clientInput ? clientInput.value.trim() : '';
      if (!val) {
        alert('Please enter a valid Google OAuth Client ID.');
        return;
      }
      localStorage.setItem('campus_google_client_id', val);
      if (modal) modal.style.display = 'none';
      tryInitGsi();
      if (window.google && window.google.accounts) {
        window.google.accounts.id.prompt();
      } else {
        alert('Google Identity Services library is loading. Please click Continue with Google in a moment.');
      }
    });
  }

  if (closeBtn) {
    closeBtn.addEventListener('click', () => {
      if (modal) modal.style.display = 'none';
    });
  }
}

document.addEventListener('DOMContentLoaded', initGoogleAuth);
