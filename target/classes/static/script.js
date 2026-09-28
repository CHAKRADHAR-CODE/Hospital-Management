/* ============ Hospital Management System - Shared JS ============ */

let currentUser = null;
try {
    currentUser = JSON.parse(sessionStorage.getItem('currentUser'));
} catch (e) { currentUser = null; }

function toast(message, type = 'info') {
    let wrap = document.querySelector('.toast-wrap');
    if (!wrap) { wrap = document.createElement('div'); wrap.className = 'toast-wrap'; document.body.appendChild(wrap); }
    const t = document.createElement('div');
    t.className = 'toast toast-' + type;
    t.textContent = message;
    wrap.appendChild(t);
    setTimeout(() => { t.style.opacity = '0'; t.style.transition = 'opacity 0.3s'; setTimeout(() => t.remove(), 300); }, 3500);
}

async function api(url, method = 'GET', body = null) {
    const opts = { method, headers: {} };
    if (body !== null) {
        opts.headers['Content-Type'] = 'application/json';
        opts.body = JSON.stringify(body);
    }
    const res = await fetch(url, opts);
    const ct = res.headers.get('content-type') || '';
    let data = null;
    if (ct.includes('application/json')) { try { data = await res.json(); } catch (e) { data = null; } }
    else { try { data = await res.text(); } catch (e) {} }
    if (!res.ok) {
        const msg = (data && data.message) ? data.message : ('Request failed (' + res.status + ')');
        throw Object.assign(new Error(msg), { status: res.status, data });
    }
    return data;
}

function esc(s) {
    if (s === null || s === undefined) return '';
    return String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function logout() {
    sessionStorage.removeItem('currentUser');
    window.location.href = 'index.html';
}

/* ============ LOGIN PAGE LOGIC ============ */
(function initLogin() {
    if (!document.getElementById('loginForm')) return;

    const roleInfo = {
        'Doctor': {
            heading: 'Doctor Sign In',
            subtitle: 'Access patient records, clinical notes and digital prescriptions',
            button: 'Sign In as Doctor'
        },
        'Patient': {
            heading: 'Patient Sign In',
            subtitle: 'Access your appointments, reports and prescriptions',
            button: 'Sign In as Patient'
        },
        'Receptionist': {
            heading: 'Receptionist Sign In',
            subtitle: 'Manage patients and hospital appointments',
            button: 'Sign In as Receptionist'
        },
        'Admin': {
            heading: 'Admin Sign In',
            subtitle: 'Manage hospital staff and system information',
            button: 'Sign In as Admin'
        }
    };

    let selectedRole = 'Doctor';

    function updateUI() {
        const info = roleInfo[selectedRole];
        document.getElementById('loginHeading').textContent = info.heading;
        document.getElementById('loginSubtitle').textContent = info.subtitle;
        document.getElementById('submitText').textContent = info.button;
        document.querySelectorAll('.role-card').forEach(rc => {
            rc.classList.toggle('selected', rc.dataset.role === selectedRole);
        });
    }

    document.querySelectorAll('.role-card').forEach(card => {
        card.addEventListener('click', () => {
            selectedRole = card.dataset.role;
            updateUI();
        });
    });

    document.getElementById('togglePassword').addEventListener('click', () => {
        const pw = document.getElementById('password');
        const isPassword = pw.type === 'password';
        pw.type = isPassword ? 'text' : 'password';
        document.getElementById('eyeIcon').innerHTML = isPassword
            ? '<rect x="2" y="12" width="20" height="9" rx="4"></rect><circle cx="12" cy="16" r="2"></circle>'
            : '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle>';
    });

    document.getElementById('loginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const errEl = document.getElementById('errorMsg');
        errEl.classList.add('hidden');
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const btn = document.getElementById('submitBtn');
        btn.disabled = true;

        try {
            const result = await api('/api/auth/login', 'POST', { username, password, role: selectedRole });
            sessionStorage.setItem('currentUser', JSON.stringify(result));
            window.location.href = result.role.toLowerCase() + '.html';
        } catch (err) {
            errEl.textContent = err.message;
            errEl.classList.remove('hidden');
            btn.disabled = false;
        }
    });

    updateUI();

    // Redirect if already logged in
    if (currentUser && currentUser.role) {
        window.location.href = currentUser.role.toLowerCase() + '.html';
    }
})();
