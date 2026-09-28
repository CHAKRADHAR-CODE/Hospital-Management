/* ============ RECEPTIONIST DASHBOARD ============ */

function initNav() {
    document.querySelectorAll('.sidebar .nav-item[data-panel]').forEach(item => {
        item.addEventListener('click', () => {
            document.querySelectorAll('.sidebar .nav-item[data-panel]').forEach(i => i.classList.remove('active'));
            document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
            item.classList.add('active');
            const panel = document.getElementById('panel-' + item.dataset.panel);
            if (panel) panel.classList.add('active');
            const handlers = { search: loadSearchPatients, appointments: loadAppointments, profile: loadReceptionistProfile };
            if (handlers[item.dataset.panel]) handlers[item.dataset.panel]();
        });
    });
    document.getElementById('logoutBtn').addEventListener('click', logout);
}

function roleBadge(role) {
    const map = { SCHEDULED: 'blue', COMPLETED: 'green', CANCELLED: 'red', RESCHEDULED: 'orange' };
    return '<span class="badge badge-' + (map[role] || 'gray') + '">' + esc(role) + '</span>';
}

async function loadSummary() {
    try {
        const patients = await api('/api/patients');
        const appts = await api('/api/appointments');
        const completed = appts.filter(a => a.status === 'COMPLETED').length;
        document.getElementById('statGrid').innerHTML =
            '<div class="stat"><div class="stat-icon">🧑‍🤝‍🧑</div><div class="stat-value">' + patients.length + '</div><div class="stat-label">Total Patients</div></div>' +
            '<div class="stat"><div class="stat-icon">📅</div><div class="stat-value">' + appts.length + '</div><div class="stat-label">Appointments</div></div>' +
            '<div class="stat"><div class="stat-icon">✅</div><div class="stat-value">' + completed + '</div><div class="stat-label">Completed</div></div>';
    } catch (e) { toast(e.message, 'error'); }
}

async function loadPatientsSelect(selectId, includeEmpty) {
    try {
        const pats = await api('/api/patients');
        const sel = document.getElementById(selectId);
        sel.innerHTML = (includeEmpty ? '<option value="">Select Patient</option>' : '') +
            pats.map(p => '<option value="' + p.id + '">' + esc(p.name) + ' (ID ' + p.id + ')</option>').join('');
    } catch (e) { toast(e.message, 'error'); }
}

async function loadDoctorsSelect() {
    try {
        const docs = await api('/api/doctors');
        const sel = document.getElementById('apptDoctor');
        sel.innerHTML = '<option value="">Select Doctor</option>' +
            docs.map(d => '<option value="' + d.id + '">' + esc(d.name) + ' - ' + esc(d.specialization) + '</option>').join('');
    } catch (e) { toast(e.message, 'error'); }
}

async function loadAppointments() {
    try {
        const appts = await api('/api/appointments');
        const rows = appts.map(a => '<tr><td>' + a.id + '</td><td>' + esc(a.patient.name) + '</td><td>' + esc(a.doctor.name) + '</td><td>' + esc(a.date) + '</td><td>' + esc(a.time) + '</td><td>' + roleBadge(a.status) + '</td><td class="btn-row">' +
            '<button class="btn btn-sm btn-accent" data-complete="' + a.id + '">Complete</button>' +
            '<button class="btn btn-sm btn-danger" data-cancel="' + a.id + '">Cancel</button>' +
            '</td></tr>').join('');
        document.getElementById('apptTable').innerHTML = rows || '<tr><td colspan="7" class="empty-state">No appointments</td></tr>';
        document.querySelectorAll('[data-complete]').forEach(b => b.addEventListener('click', async () => {
            try { await api('/api/appointments/' + b.dataset.complete + '/complete', 'PUT'); toast('Appointment completed'); loadAppointments(); if (currentUser.role==='RECEPTIONIST') loadBills(); } catch (e) { toast(e.message, 'error'); }
        }));
        document.querySelectorAll('[data-cancel]').forEach(b => b.addEventListener('click', async () => {
            try { await api('/api/appointments/' + b.dataset.cancel + '/cancel', 'PUT'); toast('Appointment cancelled'); loadAppointments(); } catch (e) { toast(e.message, 'error'); }
        }));
    } catch (e) { toast(e.message, 'error'); }
}

async function loadSearchPatients() {
    const q = document.getElementById('sSearch').value;
    let url = '/api/patients'; if (q) url += '?name=' + encodeURIComponent(q);
    try {
        const pats = await api(url);
        document.getElementById('sTable').innerHTML = pats.map(p => '<tr><td>' + p.id + '</td><td>' + esc(p.name) + '</td><td>' + esc(p.age) + '</td><td>' + esc(p.gender) + '</td><td>' + esc(p.phone) + '</td><td>' + esc(p.bloodGroup) + '</td></tr>').join('') || '<tr><td colspan="6" class="empty-state">No patients found</td></tr>';
    } catch (e) { toast(e.message, 'error'); }
}

async function loadReceptionistProfile() {
    try {
        const me = await api('/api/receptionists/me');
        document.getElementById('recProfileDetail').innerHTML =
            '<div class="doctor-card"><div class="dc-head"><div class="dc-avatar">' + esc(me.name.charAt(0)) + '</div><div><div class="dc-name">' + esc(me.name) + '</div><div class="dc-spec">Receptionist ID: ' + me.id + '</div></div></div>' +
            '<div class="dc-meta">Phone: ' + esc(me.phone) + '</div>' +
            '<div class="dc-meta">Email: ' + esc(me.email) + '</div>' +
            '<div class="dc-meta">Username: ' + esc(me.username) + '</div></div>';
    } catch (e) { toast(e.message, 'error'); }
}

async function loadBills() {
    try {
        const pats = await api('/api/patients');
        let rows = '';
        for (const p of pats) {
            const bills = await api('/api/bills/patient/' + p.id);
            for (const b of bills) {
                rows += '<tr><td>' + b.id + '</td><td>' + esc(b.patient.name) + '</td><td>' + esc(b.amount) + '</td><td>' + esc(b.date) + '</td><td>' + roleBadge(b.paymentStatus) + '</td><td><button class="btn btn-sm btn-success" data-pay="' + b.id + '">Mark Paid</button></td></tr>';
            }
        }
        document.getElementById('billTable').innerHTML = rows || '<tr><td colspan="6" class="empty-state">No bills</td></tr>';
        document.querySelectorAll('[data-pay]').forEach(btn => btn.addEventListener('click', async () => {
            try { await api('/api/bills/' + btn.dataset.pay + '/status?status=PAID', 'PUT'); toast('Bill marked as paid'); loadBills(); } catch (e) { toast(e.message, 'error'); }
        }));
    } catch (e) { toast(e.message, 'error'); }
}

document.addEventListener('DOMContentLoaded', () => {
    if (!currentUser || currentUser.role !== 'RECEPTIONIST') { toast('Please login as Receptionist', 'error'); setTimeout(logout, 1200); return; }
    document.getElementById('recName').textContent = currentUser.username || 'Receptionist';
    initNav();
    loadSummary();
    loadPatientsSelect('apptPatient', true);
    loadDoctorsSelect();
    document.getElementById('apptDate').min = new Date().toISOString().slice(0, 10);
    loadAppointments();
    loadPatientsSelect('billPatient', true);
    loadBills();

    document.getElementById('regPatientBtn').addEventListener('click', async () => {
        const patient = {
            name: document.getElementById('rName').value.trim(),
            email: document.getElementById('rEmail').value.trim(),
            phone: document.getElementById('rPhone').value.trim(),
            age: document.getElementById('rAge').value ? parseInt(document.getElementById('rAge').value) : null,
            gender: document.getElementById('rGender').value,
            bloodGroup: document.getElementById('rBlood').value,
            address: document.getElementById('rAddress').value.trim()
        };
        try {
            const creds = await api('/api/patients', 'POST', {
                username: document.getElementById('rUsername').value.trim(),
                email: patient.email,
                patient: patient
            });
            const box = document.getElementById('regCreds');
            box.classList.remove('hidden');
            box.textContent = 'Patient created! Login credentials:\nUsername: ' + creds.username + '\nPassword: ' + creds.password + '\nRole: ' + creds.role;
            toast('Patient registered');
            loadPatientsSelect('apptPatient', true); loadPatientsSelect('billPatient', true); loadSummary(); loadBills();
        } catch (e) { toast(e.message, 'error'); }
    });

    document.getElementById('rcChangeBtn').addEventListener('click', async () => {
        const body = {
            currentPassword: document.getElementById('rcCurrent').value,
            newPassword: document.getElementById('rcNew').value,
            confirmPassword: document.getElementById('rcConfirm').value
        };
        try {
            await api('/api/auth/change-password', 'POST', body);
            toast('Password changed successfully', 'success');
            document.getElementById('rcCurrent').value=''; document.getElementById('rcNew').value=''; document.getElementById('rcConfirm').value='';
        } catch (e) { toast(e.message, 'error'); }
    });

    document.getElementById('sSearchBtn').addEventListener('click', loadSearchPatients);
    document.getElementById('sSearch').addEventListener('keydown', e => { if (e.key === 'Enter') loadSearchPatients(); });

    document.getElementById('apptDoctor').addEventListener('change', async () => {
        const docId = document.getElementById('apptDoctor').value;
        const info = document.getElementById('availabilityInfo');
        if (!docId) { info.textContent = ''; return; }
        try {
            const doc = await api('/api/doctors/' + docId);
            if (doc.availability && doc.availability.length) {
                info.innerHTML = '<strong>Availability:</strong> ' + doc.availability.map(a => esc(a.day) + ': ' + esc(a.startTime) + ' - ' + esc(a.endTime)).join(' | ');
            } else info.textContent = 'Doctor has not set availability.';
        } catch (e) { info.textContent = ''; }
    });

    document.getElementById('createApptBtn').addEventListener('click', async () => {
        const body = {
            patient: { id: document.getElementById('apptPatient').value },
            doctor: { id: document.getElementById('apptDoctor').value },
            date: document.getElementById('apptDate').value,
            time: document.getElementById('apptTime').value,
            reason: document.getElementById('apptReason').value
        };
        try {
            await api('/api/appointments', 'POST', body);
            toast('Appointment created');
            loadAppointments(); loadSummary();
            document.getElementById('apptDate').value = ''; document.getElementById('apptTime').value = ''; document.getElementById('apptReason').value = '';
        } catch (e) { toast(e.message, 'error'); }
    });

    document.getElementById('billPatient').addEventListener('change', async () => {
        const pid = document.getElementById('billPatient').value;
        const sel = document.getElementById('billAppt');
        sel.innerHTML = '<option value="">None</option>';
        if (!pid) return;
        try {
            const appts = await api('/api/appointments/patient/' + pid);
            appts.filter(a => a.status !== 'CANCELLED').forEach(a => {
                sel.innerHTML += '<option value="' + a.id + '">' + esc(a.date) + ' ' + esc(a.time) + ' - ' + esc(a.doctor.name) + ' (' + a.status + ')</option>';
            });
        } catch (e) {}
    });

    document.getElementById('genBillBtn').addEventListener('click', async () => {
        const pid = document.getElementById('billPatient').value;
        if (!pid) { toast('Select a patient', 'error'); return; }
        const body = {
            patient: { id: pid },
            appointment: document.getElementById('billAppt').value ? { id: document.getElementById('billAppt').value } : null,
            amount: parseFloat(document.getElementById('billAmount').value),
            description: document.getElementById('billDesc').value
        };
        try {
            await api('/api/bills', 'POST', body);
            toast('Bill generated');
            loadBills(); loadSummary();
            document.getElementById('billAmount').value = ''; document.getElementById('billDesc').value = '';
        } catch (e) { toast(e.message, 'error'); }
    });
});
