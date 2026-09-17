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
// Google Identity Services (Sign in with Google)
// ==========================================

// Configurable Google OAuth Client ID:
// If you create a Web Client ID in Google Cloud Console, set it here:
window.GOOGLE_CLIENT_ID = window.GOOGLE_CLIENT_ID || '';

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

function handleGoogleCredentialResponse(response) {
  const payload = parseJwt(response.credential);
  if (!payload) {
    alert('Failed to parse Google login credential token.');
    return;
  }

  const role = roleField.value || 'student';
  const profile = {
    name: payload.name || payload.email.split('@')[0],
    email: payload.email,
    picture: payload.picture || '',
    id: role === 'student' ? 'S101' : 'ADM-014',
    course: role === 'student' ? 'B.Tech Computer Science & Engg (KTU)' : 'Academic Administration',
    year: role === 'student' ? '2nd Year · Semester 3' : 'Administrator',
    advisor: 'Dr. Joseph Kurian',
    authProvider: 'google'
  };

  sessionStorage.setItem('ridgeview_role', role);
  sessionStorage.setItem('ridgeview_profile', JSON.stringify(profile));
  window.location.href = role === 'student' ? 'student-dashboard.html' : 'admin-dashboard.html';
}

window.handleGoogleCredentialResponse = handleGoogleCredentialResponse;

function initGoogleAuth() {
  const googleBtn = document.getElementById('googleLoginBtn');
  if (!googleBtn) return;

  function tryInitGsi() {
    if (window.google && window.google.accounts && window.GOOGLE_CLIENT_ID) {
      window.google.accounts.id.initialize({
        client_id: window.GOOGLE_CLIENT_ID,
        callback: handleGoogleCredentialResponse,
        auto_select: false
      });
      const wrapper = document.getElementById('googleGsiWrapper');
      if (wrapper) {
        window.google.accounts.id.renderButton(wrapper, {
          theme: 'outline',
          size: 'large',
          width: 320,
          text: 'signin_with',
          shape: 'rectangular'
        });
      }
    }
  }

  if (window.google && window.google.accounts) {
    tryInitGsi();
  } else {
    window.addEventListener('load', tryInitGsi);
  }

  googleBtn.addEventListener('click', () => {
    // If real Client ID is initialized, prompt GIS
    if (window.google && window.google.accounts && window.GOOGLE_CLIENT_ID) {
      window.google.accounts.id.prompt();
      return;
    }

    // 1-Click Interactive Google Sign-In
    const role = roleField.value || 'student';
    const suggestedEmail = role === 'student' ? 'dev.nikhilbiju@gmail.com' : 'admin.nikhilbiju@gmail.com';
    const email = prompt('Sign in with Google:\nEnter your Google Account email:', suggestedEmail);
    if (!email || !email.trim()) return;

    const rawName = email.split('@')[0].replace(/[\._]/g, ' ');
    const name = rawName.replace(/\b\w/g, l => l.toUpperCase());

    const profile = {
      name: name,
      email: email.trim(),
      picture: 'https://lh3.googleusercontent.com/a/default-user=s96-c',
      id: role === 'student' ? 'S101' : 'ADM-014',
      course: role === 'student' ? 'B.Tech Computer Science & Engg (KTU)' : 'Academic Administration',
      year: role === 'student' ? '2nd Year · Semester 3' : 'Administrator',
      advisor: 'Dr. Joseph Kurian',
      authProvider: 'google'
    };

    const label = document.getElementById('googleBtnLabel');
    if (label) label.textContent = 'Connecting with Google...';
    googleBtn.disabled = true;

    setTimeout(() => {
      sessionStorage.setItem('ridgeview_role', role);
      sessionStorage.setItem('ridgeview_profile', JSON.stringify(profile));
      window.location.href = role === 'student' ? 'student-dashboard.html' : 'admin-dashboard.html';
    }, 400);
  });
}

document.addEventListener('DOMContentLoaded', initGoogleAuth);
