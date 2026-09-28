/* ============ ADMIN DASHBOARD ============ */

function initNav() {
    document.querySelectorAll('.sidebar .nav-item[data-panel]').forEach(item => {
        item.addEventListener('click', () => {
            document.querySelectorAll('.sidebar .nav-item[data-panel]').forEach(i => i.classList.remove('active'));
            document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
            item.classList.add('active');
            const panel = document.getElementById('panel-' + item.dataset.panel);
            if (panel) panel.classList.add('active');
            const handlers = {
                doctors: loadDoctors,
                receptionists: loadReceptionists,
                departments: loadDepartments,
                patients: loadPatients,
                appointments: loadAppointments,
                reports: loadReport
            };
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
        const s = await api('/api/dashboard/summary');
        const items = [
            ['🧑‍🤝‍🧑', s.totalPatients, 'Total Patients'],
            ['🩺', s.totalDoctors, 'Total Doctors'],
            ['🖥️', s.totalReceptionists, 'Receptionists'],
            ['🏥', s.totalDepartments, 'Departments'],
            ['📅', s.totalAppointments, 'Appointments'],
            ['✅', s.completedAppointments, 'Completed'],
            ['⏳', s.scheduledAppointments, 'Scheduled'],
            ['🧾', s.totalBills, 'Bills']
        ];
        document.getElementById('statGrid').innerHTML = items.map(i =>
            '<div class="stat"><div class="stat-icon">' + i[0] + '</div><div class="stat-value">' + i[1] + '</div><div class="stat-label">' + i[2] + '</div></div>'
        ).join('');
    } catch (e) { toast(e.message, 'error'); }
}

async function loadDepartments(selectId) {
    try {
        const depts = await api('/api/departments');
        if (selectId) {
            const sel = document.getElementById(selectId);
            if (sel) sel.innerHTML = '<option value="">Select Department</option>' + depts.map(d => '<option value="' + d.id + '">' + esc(d.name) + '</option>').join('');
        }
        const rows = depts.map(d => '<tr><td>' + d.id + '</td><td>' + esc(d.name) + '</td><td>' + esc(d.description) + '</td><td>' + (d.doctors ? d.doctors.length : 0) + '</td><td><button class="btn btn-danger btn-sm" data-del-dept="' + d.id + '">Delete</button></td></tr>').join('');
        document.getElementById('departmentTable').innerHTML = rows || '<tr><td colspan="5" class="empty-state">No departments</td></tr>';
        document.querySelectorAll('[data-del-dept]').forEach(b => b.addEventListener('click', async () => {
            try { await api('/api/departments/' + b.dataset.delDept, 'DELETE'); toast('Department deleted'); loadDepartments(); } catch (e) { toast(e.message, 'error'); }
        }));
    } catch (e) { toast(e.message, 'error'); }
}

async function loadDoctors() {
    try {
        await loadDepartments('docDept');
        const docs = await api('/api/doctors');
        const rows = docs.map(d => '<tr><td>' + d.id + '</td><td>' + esc(d.name) + '</td><td>' + esc(d.specialization) + '</td><td>' + esc((d.department && d.department.name) || 'N/A') + '</td><td>' + esc(d.phone) + '</td><td><button class="btn btn-danger btn-sm" data-del-doc="' + d.id + '">Delete</button></td></tr>').join('');
        document.getElementById('doctorTable').innerHTML = rows || '<tr><td colspan="6" class="empty-state">No doctors</td></tr>';
        document.querySelectorAll('[data-del-doc]').forEach(b => b.addEventListener('click', async () => {
            try { await api('/api/doctors/' + b.dataset.delDoc, 'DELETE'); toast('Doctor removed'); loadDoctors(); } catch (e) { toast(e.message, 'error'); }
        }));
    } catch (e) { toast(e.message, 'error'); }
}

async function loadReceptionists() {
    try {
        const recs = await api('/api/receptionists');
        const rows = recs.map(r => '<tr><td>' + r.id + '</td><td>' + esc(r.name) + '</td><td>' + esc(r.email) + '</td><td>' + esc(r.phone) + '</td><td><button class="btn btn-danger btn-sm" data-del-rec="' + r.id + '">Delete</button></td></tr>').join('');
        document.getElementById('receptionistTable').innerHTML = rows || '<tr><td colspan="5" class="empty-state">No receptionists</td></tr>';
        document.querySelectorAll('[data-del-rec]').forEach(b => b.addEventListener('click', async () => {
            try { await api('/api/receptionists/' + b.dataset.delRec, 'DELETE'); toast('Receptionist removed'); loadReceptionists(); } catch (e) { toast(e.message, 'error'); }
        }));
    } catch (e) { toast(e.message, 'error'); }
}

async function loadPatients() {
    try {
        const q = document.getElementById('patientSearch').value;
        let url = '/api/patients';
        if (q) url += '?name=' + encodeURIComponent(q);
        const pats = await api(url);
        const rows = pats.map(p => '<tr><td>' + p.id + '</td><td>' + esc(p.name) + '</td><td>' + esc(p.age) + '</td><td>' + esc(p.gender) + '</td><td>' + esc(p.phone) + '</td><td>' + esc(p.bloodGroup) + '</td></tr>').join('');
        document.getElementById('patientTable').innerHTML = rows || '<tr><td colspan="6" class="empty-state">No patients found</td></tr>';
    } catch (e) { toast(e.message, 'error'); }
}

async function loadAppointments() {
    try {
        const appts = await api('/api/appointments');
        const rows = appts.map(a => '<tr><td>' + a.id + '</td><td>' + esc(a.patient.name) + '</td><td>' + esc(a.doctor.name) + '</td><td>' + esc(a.date) + '</td><td>' + esc(a.time) + '</td><td>' + roleBadge(a.status) + '</td></tr>').join('');
        document.getElementById('appointmentTable').innerHTML = rows || '<tr><td colspan="6" class="empty-state">No appointments</td></tr>';
    } catch (e) { toast(e.message, 'error'); }
}

async function loadReport() {
    try {
        const r = await api('/api/dashboard/reports');
        document.getElementById('hospitalReport').innerHTML =
            '<div class="stat-grid">' +
            [
                ['🧑‍🤝‍🧑', r.patients, 'Patients'],
                ['🩺', r.doctors, 'Doctors'],
                ['🖥️', r.receptionists, 'Receptionists'],
                ['🏥', r.departments, 'Departments'],
                ['📅', r.appointments, 'Appointments'],
                ['🧾', r.bills, 'Bills'],
                ['💬', r.feedback, 'Feedback']
            ].map(i => '<div class="stat"><div class="stat-icon">' + i[0] + '</div><div class="stat-value">' + i[1] + '</div><div class="stat-label">' + i[2] + '</div></div>').join('') +
            '</div>';
    } catch (e) { toast(e.message, 'error'); }
}

document.addEventListener('DOMContentLoaded', () => {
    if (!currentUser || currentUser.role !== 'ADMIN') { toast('Please login as Admin', 'error'); setTimeout(logout, 1200); return; }
    document.getElementById('adminName').textContent = currentUser.username || 'Admin';
    initNav();
    loadSummary();
    loadDepartments();
    loadDoctors();
    loadReceptionists();

    document.getElementById('addDoctorBtn').addEventListener('click', async () => {
        const doctor = {
            name: document.getElementById('docName').value.trim(),
            specialization: document.getElementById('docSpec').value.trim(),
            email: document.getElementById('docEmail').value.trim(),
            phone: document.getElementById('docPhone').value.trim(),
            qualification: document.getElementById('docQual').value.trim(),
            yearsOfExperience: document.getElementById('docExp').value ? parseInt(document.getElementById('docExp').value) : null,
            bio: document.getElementById('docBio').value.trim()
        };
        const body = {
            username: document.getElementById('docUsername').value.trim(),
            email: doctor.email,
            departmentId: document.getElementById('docDept').value ? document.getElementById('docDept').value : null,
            doctor: doctor
        };
        try {
            const creds = await api('/api/doctors', 'POST', body);
            const box = document.getElementById('doctorCreds');
            box.classList.remove('hidden');
            box.textContent = 'Doctor created! Login credentials:\nUsername: ' + creds.username + '\nPassword: ' + creds.password + '\nRole: ' + creds.role;
            toast('Doctor added successfully');
            loadDoctors(); updateSummaryAfter();
        } catch (e) { toast(e.message, 'error'); }
    });

    document.getElementById('addRecBtn').addEventListener('click', async () => {
        const receptionist = {
            name: document.getElementById('recName').value.trim(),
            email: document.getElementById('recEmail').value.trim(),
            phone: document.getElementById('recPhone').value.trim()
        };
        try {
            const creds = await api('/api/receptionists', 'POST', {
                username: document.getElementById('recUsername').value.trim(),
                email: receptionist.email,
                receptionist: receptionist
            });
            const box = document.getElementById('recCreds');
            box.classList.remove('hidden');
            box.textContent = 'Receptionist created! Login credentials:\nUsername: ' + creds.username + '\nPassword: ' + creds.password + '\nRole: ' + creds.role;
            toast('Receptionist added successfully');
            loadReceptionists(); updateSummaryAfter();
        } catch (e) { toast(e.message, 'error'); }
    });

    document.getElementById('addDeptBtn').addEventListener('click', async () => {
        try {
            await api('/api/departments', 'POST', {
                name: document.getElementById('deptName').value.trim(),
                description: document.getElementById('deptDesc').value.trim()
            });
            toast('Department added'); loadDepartments(); loadDepartments('docDept'); updateSummaryAfter();
            document.getElementById('deptName').value = ''; document.getElementById('deptDesc').value = '';
        } catch (e) { toast(e.message, 'error'); }
    });

    document.getElementById('patientSearchBtn').addEventListener('click', loadPatients);
    document.getElementById('patientSearch').addEventListener('keydown', e => { if (e.key === 'Enter') loadPatients(); });
});

async function updateSummaryAfter() { loadSummary(); }
