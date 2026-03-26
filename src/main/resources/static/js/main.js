/* ═══════════════════════════════════════════════════════════════
   BookVault — Main JS
   ═══════════════════════════════════════════════════════════════ */

// ── Toast ───────────────────────────────────────────────────────
const Toast = (() => {
  const container = document.getElementById('toast-container') || (() => {
    const el = document.createElement('div');
    el.id = 'toast-container';
    document.body.appendChild(el);
    return el;
  })();

  function show(message, type = 'success', duration = 4000) {
    const icon = type === 'success' ? '✓' : '✕';
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
      toast.style.animation = 'toastIn 0.3s reverse forwards';
      setTimeout(() => toast.remove(), 300);
    }, duration);
  }

  return { success: (m) => show(m, 'success'), error: (m) => show(m, 'error') };
})();

// ── CSRF Helper ─────────────────────────────────────────────────
function getCsrfToken() {
  const meta = document.querySelector('meta[name="_csrf"]');
  return meta ? meta.content : null;
}

// ── API Helper ──────────────────────────────────────────────────
const API = {
  async request(method, url, body = null, isFormData = false) {
    const jwt = getJwtFromCookie();
    const headers = {};
    if (jwt) headers['Authorization'] = `Bearer ${jwt}`;
    if (!isFormData) headers['Content-Type'] = 'application/json';

    const opts = { method, headers, credentials: 'include' };
    if (body) opts.body = isFormData ? body : JSON.stringify(body);

    const res = await fetch(url, opts);
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || data.message || 'Request failed');
    return data;
  },

  get: (url)             => API.request('GET', url),
  post: (url, body)      => API.request('POST', url, body),
  put: (url, body)       => API.request('PUT', url, body),
  delete: (url)          => API.request('DELETE', url),
  postForm: (url, form)  => API.request('POST', url, form, true),
  putForm: (url, form)   => API.request('PUT', url, form, true),
};

function getJwtFromCookie() {
  const match = document.cookie.match(/jwt_token=([^;]+)/);
  return match ? match[1] : null;
}

// ── Auth State ──────────────────────────────────────────────────
const AuthState = {
  user: null,
  async init() {
    const jwt = getJwtFromCookie();
    if (!jwt) return;
    try {
      const res = await API.get('/api/auth/me');
      this.user = res.data;
    } catch (_) { /* not authenticated */ }
  },
  isLoggedIn() { return !!this.user; },
  isAdmin()    { return this.user?.role === 'ADMIN'; },
};

// ── Login Form ──────────────────────────────────────────────────
async function submitLogin(e) {
  e.preventDefault();
  const form = e.target;
  const btn  = form.querySelector('[type=submit]');
  const err  = document.getElementById('loginError');

  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span> Signing in…';
  if (err) err.classList.add('hidden');

  try {
    await API.post('/api/auth/login', {
      usernameOrEmail: form.usernameOrEmail.value.trim(),
      password: form.password.value,
    });
    Toast.success('Welcome back!');
    const redirect = new URLSearchParams(window.location.search).get('redirect') || '/';
    window.location.href = redirect;
  } catch (e) {
    if (err) { err.textContent = e.message; err.classList.remove('hidden'); }
    btn.disabled = false;
    btn.textContent = 'Sign In';
  }
}

// ── Register Form ───────────────────────────────────────────────
async function submitRegister(e) {
  e.preventDefault();
  const form = e.target;
  const btn  = form.querySelector('[type=submit]');
  const err  = document.getElementById('registerError');

  if (form.password.value !== form.confirmPassword.value) {
    if (err) { err.textContent = 'Passwords do not match'; err.classList.remove('hidden'); }
    return;
  }

  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span> Creating account…';
  if (err) err.classList.add('hidden');

  try {
    await API.post('/api/auth/register', {
      username: form.username.value.trim(),
      email: form.email.value.trim(),
      password: form.password.value,
    });
    Toast.success('Account created! Welcome to BookVault.');
    window.location.href = '/';
  } catch (e) {
    if (err) { err.textContent = e.message; err.classList.remove('hidden'); }
    btn.disabled = false;
    btn.textContent = 'Create Account';
  }
}

// ── Logout ──────────────────────────────────────────────────────
async function logout() {
  try {
    await API.post('/api/auth/logout');
  } catch (_) {}
  document.cookie = 'jwt_token=; Max-Age=0; path=/';
  window.location.href = '/login';
}

// ── Scroll to top ───────────────────────────────────────────────
const scrollTopBtn = document.getElementById('scrollTop');
if (scrollTopBtn) {
  window.addEventListener('scroll', () => {
    scrollTopBtn.classList.toggle('visible', window.scrollY > 400);
  });
  scrollTopBtn.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
}

// ── Modal helpers ───────────────────────────────────────────────
function openModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.remove('hidden');
}

function closeModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.add('hidden');
}

// Close modals on backdrop click
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-backdrop')) {
    e.target.classList.add('hidden');
  }
});

// ── Book Search ─────────────────────────────────────────────────
function initSearch() {
  const searchInput = document.getElementById('searchInput');
  if (!searchInput) return;

  let debounceTimer;
  searchInput.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      const q = searchInput.value.trim();
      if (q.length >= 2) {
        window.location.href = `/books?query=${encodeURIComponent(q)}`;
      } else if (q.length === 0) {
        window.location.href = '/books';
      }
    }, 600);
  });

  // Also handle Enter
  searchInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      clearTimeout(debounceTimer);
      const q = searchInput.value.trim();
      if (q) window.location.href = `/books?query=${encodeURIComponent(q)}`;
    }
  });
}

// ── Confirm Delete ──────────────────────────────────────────────
function confirmDelete(bookId, bookTitle) {
  const modal = document.getElementById('deleteModal');
  if (!modal) return;
  document.getElementById('deleteBookTitle').textContent = bookTitle;
  document.getElementById('confirmDeleteBtn').onclick = () => deleteBook(bookId);
  modal.classList.remove('hidden');
}

async function deleteBook(id) {
  try {
    await API.delete(`/api/v1/books/${id}`);
    Toast.success('Book deleted successfully');
    closeModal('deleteModal');
    setTimeout(() => window.location.reload(), 800);
  } catch (e) {
    Toast.error(e.message);
  }
}

// ── File Upload Preview ─────────────────────────────────────────
function initFileUpload() {
  const pdfInput = document.getElementById('pdfFile');
  const pdfLabel = document.getElementById('pdfLabel');
  if (!pdfInput || !pdfLabel) return;

  pdfInput.addEventListener('change', () => {
    const file = pdfInput.files[0];
    if (file) {
      const size = (file.size / 1024 / 1024).toFixed(2);
      pdfLabel.innerHTML = `<span>📄</span><span>${file.name} (${size} MB)</span>`;
      pdfLabel.style.borderColor = 'var(--gold-500)';
    }
  });

  // Drag-and-drop
  pdfLabel.addEventListener('dragover', (e) => {
    e.preventDefault();
    pdfLabel.style.borderColor = 'var(--gold-400)';
    pdfLabel.style.background = 'var(--gold-glow)';
  });

  pdfLabel.addEventListener('dragleave', () => {
    pdfLabel.style.borderColor = '';
    pdfLabel.style.background = '';
  });

  pdfLabel.addEventListener('drop', (e) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file && file.type === 'application/pdf') {
      pdfInput.files = e.dataTransfer.files;
      const size = (file.size / 1024 / 1024).toFixed(2);
      pdfLabel.innerHTML = `<span>📄</span><span>${file.name} (${size} MB)</span>`;
    } else {
      Toast.error('Only PDF files are allowed');
    }
    pdfLabel.style.borderColor = '';
    pdfLabel.style.background = '';
  });
}

// ── Book Form Submit (Admin) ────────────────────────────────────
async function submitBookForm(e, bookId = null) {
  e.preventDefault();
  const form = e.target;
  const btn  = form.querySelector('[type=submit]');
  const err  = document.getElementById('bookFormError');

  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span> Saving…';
  if (err) err.classList.add('hidden');

  const formData = new FormData();
  const bookData = {
    title:          form.title.value.trim(),
    author:         form.author.value.trim(),
    isbn:           form.isbn?.value.trim() || null,
    versionRelease: form.versionRelease.value.trim(),
    description:    form.description?.value.trim() || null,
    genre:          form.genre?.value || null,
    publisher:      form.publisher?.value.trim() || null,
    language:       form.language?.value || 'English',
    pageCount:      form.pageCount?.value ? parseInt(form.pageCount.value) : null,
    coverImageUrl:  form.coverImageUrl?.value.trim() || null,
    isPublic:       form.isPublic?.checked ?? true,
  };

  formData.append('book', new Blob([JSON.stringify(bookData)], { type: 'application/json' }));

  const pdfFile = form.pdfFile?.files[0];
  if (pdfFile) formData.append('pdf', pdfFile);

  try {
    if (bookId) {
      await API.putForm(`/api/v1/books/${bookId}`, formData);
      Toast.success('Book updated successfully!');
    } else {
      await API.postForm('/api/v1/books', formData);
      Toast.success('Book created successfully!');
    }
    setTimeout(() => window.location.href = '/admin/books', 700);
  } catch (e) {
    if (err) { err.textContent = e.message; err.classList.remove('hidden'); }
    btn.disabled = false;
    btn.textContent = bookId ? 'Update Book' : 'Create Book';
  }
}

// ── Init ────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  initSearch();
  initFileUpload();
});
