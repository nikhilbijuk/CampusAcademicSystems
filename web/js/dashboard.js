// Shared logic for student-dashboard.html and admin-dashboard.html.

(function () {
  // ---- Section switching (sidebar tabs) ----
  const links = document.querySelectorAll('.side-link');
  const sections = document.querySelectorAll('.dash-section');
  const pageTitle = document.getElementById('pageTitle');

  links.forEach((link) => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      const target = link.getAttribute('data-target');

      links.forEach((l) => l.classList.remove('active'));
      link.classList.add('active');

      sections.forEach((s) => s.classList.toggle('active', s.id === target));
      if (pageTitle) pageTitle.textContent = link.textContent.trim();
      history.replaceState(null, '', `#${target}`);
    });
  });

  // Open the section matching the URL hash on load, if any.
  const initial = window.location.hash.replace('#', '');
  if (initial) {
    const match = document.querySelector(`.side-link[data-target="${initial}"]`);
    if (match) match.click();
  }

  // ---- Fill in the signed-in user's name from the login step ----
  const guard = document.body.getAttribute('data-guard');
  const rawProfile = sessionStorage.getItem('ridgeview_profile');
  const role = sessionStorage.getItem('ridgeview_role');

  if (rawProfile && role === guard) {
    try {
      const profile = JSON.parse(rawProfile);
      const nameEl = document.getElementById('sideUserName');
      const initialsEl = document.getElementById('avatarInitials');
      const topAvatarEl = document.getElementById('topbarAvatar');
      const sideRoleEl = document.getElementById('sideUserRole');
      const topbarSub = document.getElementById('topbarSub');

      const initials = profile.name ? profile.name.split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase() : 'RS';

      if (nameEl && profile.name) nameEl.textContent = profile.name;
      if (initialsEl) initialsEl.textContent = initials;
      if (topAvatarEl) topAvatarEl.textContent = initials;
      if (sideRoleEl && (profile.year || profile.role)) sideRoleEl.textContent = profile.year || profile.role;
      if (topbarSub && (profile.course || profile.role)) {
        topbarSub.textContent = `${profile.course || profile.role} ${profile.year ? '· ' + profile.year : ''}`;
      }

      // Populate student profile table
      const pName = document.getElementById('pName');
      const pId = document.getElementById('pId');
      const pCourse = document.getElementById('pCourse');
      const pSemester = document.getElementById('pSemester');
      const pEmail = document.getElementById('pEmail');
      const pAdvisor = document.getElementById('pAdvisor');

      if (pName && profile.name) pName.textContent = profile.name;
      if (pId && profile.id) pId.textContent = profile.id;
      if (pCourse && profile.course) pCourse.textContent = profile.course;
      if (pSemester && profile.year) pSemester.textContent = profile.year;
      if (pEmail && profile.email) pEmail.textContent = profile.email;
      if (pAdvisor && profile.advisor) pAdvisor.textContent = profile.advisor;
    } catch (err) {
      // Ignore malformed session data and fall back to the demo defaults already in the HTML.
    }
  }
  // Note: no hard redirect if there's no session — this keeps the dashboard
  // easy to preview directly. Wire this up to a real auth check once the
  // Java backend issues sessions/tokens.

  // ---- Logout ----
  const logoutBtn = document.getElementById('logoutBtn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      sessionStorage.removeItem('ridgeview_role');
      sessionStorage.removeItem('ridgeview_profile');
    });
  }
})();
