// Login page logic.
// This ships with a small in-browser demo directory so the portal is fully
// clickable without a live backend. Swap `authenticate()` for a fetch() call
// to LoginServlet (see backend/) when the Java backend is deployed.

const KNOWN_USERS = {
  student: [
    {
      ids: ['s101', 's101@adishankara.ac.in', 'rahul@adishankara.ac.in', 's101@ridgeview.edu', 'rahul@ridgeview.edu'],
      password: 'student123',
      profile: { name: 'Rahul Sharma', id: 'S101', course: 'B.Tech Computer Science & Engg (KTU)', year: '2nd Year · Semester 3', email: 'rahul.s101@adishankara.ac.in', advisor: 'Dr. Joseph Kurian' }
    },
    {
      ids: ['s102', 's102@adishankara.ac.in', 'priya@adishankara.ac.in', 's102@ridgeview.edu', 'priya@ridgeview.edu'],
      password: 'student123',
      profile: { name: 'Priya Nair', id: 'S102', course: 'B.Tech Computer Science & Engg (KTU)', year: '2nd Year · Semester 3', email: 'priya.s102@adishankara.ac.in', advisor: 'Dr. Joseph Kurian' }
    },
    {
      ids: ['student@adishankara.ac.in', 'aditi@adishankara.ac.in', 'student@ridgeview.edu', 'asi2023cs041', 'aditi@ridgeview.edu'],
      password: 'student123',
      profile: { name: 'Aditi Menon', id: 'ASI2023CS041', course: 'B.Tech Computer Science', year: '3rd Year · Semester 5', email: 'student@adishankara.ac.in', advisor: 'Dr. Leena Fernandes' }
    },
    {
      ids: ['dev.nikhilbiju@gmail.com', 'nikhil@gmail.com', 'nikhil'],
      password: 'student123',
      profile: { name: 'Nikhil Biju', id: 'S103', course: 'B.Tech Computer Science & Engg (KTU)', year: '2nd Year · Semester 3', email: 'dev.nikhilbiju@gmail.com', advisor: 'Dr. Joseph Kurian' }
    }
  ],
  admin: [
    {
      ids: ['admin@adishankara.ac.in', 'admin@ridgeview.edu', 'adm-014', 'admin', 'admin123'],
      password: 'admin123',
      profile: { name: 'Rahul Verma', id: 'ADM-014', role: 'Campus Administrator', email: 'admin@adishankara.ac.in' }
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
    ? 'Sign in with your student ID or email to access campus facilities, mess billing, and clearances.'
    : 'Sign in with your staff ID or email to manage campus facilities and operations.';
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
