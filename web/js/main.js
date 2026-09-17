// Home page — mobile nav toggle
(function () {
  const toggle = document.querySelector('.nav-toggle');
  const links = document.querySelector('.nav-links');
  if (!toggle || !links) return;

  toggle.addEventListener('click', () => {
    const open = links.style.display === 'flex';
    links.style.display = open ? 'none' : 'flex';
    links.style.cssText += open ? '' : `
      position:absolute; top:72px; left:0; right:0; background:#FAFAF8;
      flex-direction:column; padding:20px 28px; border-bottom:1px solid #E4E1D8; gap:18px;
    `;
  });
})();
