/* ============ PATIENT DASHBOARD ============ */

let myPatientId = null;

function initNav() {
    document.querySelectorAll('.sidebar .nav-item[data-panel]').forEach(item => {
        item.addEventListener('click', () => {
            document.querySelectorAll('.sidebar .nav-item[data-panel]').forEach(i => i.classList.remove('active'));
            document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
            item.classList.add('active');
            const panel = document.getElementById('panel-' + item.dataset.panel);
            if (panel) panel.classList.add('active');
            const handlers = {
                profile: loadProfile,
                appointments: loadMyAppointments,
                prescriptions: loadPrescriptions,
                reports: loadReports,
                consultations: loadConsultations,
                bills: loadBills,
                searchDocs: searchDoctors,
                availDocs: populateAvailDoctors,
                upload: noop,
                feedback: populateFbDoctors
            };
            if (handlers[item.dataset.panel]) handlers[item.dataset.panel]();
        });
    });
    document.getElementById('logoutBtn').addEventListener('click', logout);
}

function noop() {}

function roleBadge(role) {
    const map = { SCHEDULED: 'blue', COMPLETED: 'green', CANCELLED: 'red', RESCHEDULED: 'orange' };
    return '<span class="badge badge-' + (map[role] || 'gray') + '">' + esc(role) + '</span>';
}

async function initProfile() {
    const me = await api('/api/patients/me');
    myPatientId = me.id;
    return me;
}

async function loadStats() {
    try {
        const me = await initProfile();
        document.getElementById('patientName').textContent = me.name;
        document.getElementById('uName').value = me.name;
        document.getElementById('uPhone').value = me.phone;
        const appts = await api('/api/appointments/patient/' + myPatientId);
        const rxs = await api('/api/prescriptions/patient/' + myPatientId);
        const bills = await api('/api/bills/patient/' + myPatientId);
        const upcoming = appts.filter(a => a.status === 'SCHEDULED');
        document.getElementById('statGrid').innerHTML =
            '<div class="stat"><div class="stat-icon">📅</div><div class="stat-value">' + appts.length + '</div><div class="stat-label">Appointments</div></div>' +
            '<div class="stat"><div class="stat-icon">💊</div><div class="stat-value">' + rxs.length + '</div><div class="stat-label">Prescriptions</div></div>' +
            '<div class="stat"><div class="stat-icon">🧾</div><div class="stat-value">' + bills.length + '</div><div class="stat-label">Bills</div></div>';
        document.getElementById('upcomingAppts').innerHTML = upcoming.length ?
            upcoming.map(a => '<div class="list-item"><div class="li-main"><div class="li-title">' + esc(a.doctor.name) + '</div><div class="li-sub">' + esc(a.date) + ' · ' + esc(a.time) + '</div></div></div>').join('')
            : '<div class="empty-state">No upcoming appointments</div>';
    } catch (e) { toast(e.message, 'error'); }
}

async function loadProfile() {
    const me = await initProfile();
    document.getElementById('profileDetail').innerHTML =
        '<div class="doctor-card"><div class="dc-head"><div class="dc-avatar">' + esc(me.name.charAt(0)) + '</div><div><div class="dc-name">' + esc(me.name) + '</div><div class="dc-spec">Patient ID: ' + me.id + '</div></div></div>' +
        '<div class="dc-meta">Age: ' + esc(me.age) + ' · Gender: ' + esc(me.gender) + '</div>' +
        '<div class="dc-meta">Blood Group: ' + esc(me.bloodGroup) + '</div>' +
        '<div class="dc-meta">Phone: ' + esc(me.phone) + '</div>' +
        '<div class="dc-meta">Email: ' + esc(me.email) + '</div>' +
        '<div class="dc-meta">Address: ' + esc(me.address) + '</div>' +
        '<div class="dc-meta">Registered: ' + esc(me.registrationDate) + '</div></div>';
}

async function loadMyAppointments() {
    try {
        const appts = await api('/api/appointments/patient/' + myPatientId);
        document.getElementById('myApptTable').innerHTML = appts.map(a =>
            '<tr><td>' + a.id + '</td><td>' + esc(a.doctor.name) + '</td><td>' + esc(a.doctor.specialization) + '</td><td>' + esc(a.date) + '</td><td>' + esc(a.time) + '</td><td>' + roleBadge(a.status) + '</td></tr>').join('') ||
            '<tr><td colspan="6" class="empty-state">No appointments</td></tr>';
    } catch (e) { toast(e.message, 'error'); }
}

async function loadPrescriptions() {
    try {
        const rxs = await api('/api/prescriptions/patient/' + myPatientId);
        document.getElementById('prescriptionList').innerHTML = rxs.map(r =>
            '<div class="list-item"><div class="li-main"><div><div class="li-title">💊 ' + esc(r.medicine) + '</div><div class="li-sub">Dosage: ' + esc(r.dosage) + ' | Frequency: ' + esc(r.frequency) + ' | Duration: ' + esc(r.duration) + '</div><div class="li-sub">Doctor: ' + esc(r.doctor.name) + ' | ' + esc(r.date) + '</div><div class="li-sub">' + esc(r.instructions) + '</div></div></div></div>').join('') ||
            '<div class="empty-state">No prescriptions</div>';
    } catch (e) { toast(e.message, 'error'); }
}

async function loadReports() {
    try {
        const reports = await api('/api/reports/patient/' + myPatientId);
        document.getElementById('reportList').innerHTML = reports.map(r =>
            '<div class="list-item"><div class="li-main"><div><div class="li-title">' + esc(r.title) + '</div><div class="li-sub">' + esc(r.reportType) + ' · ' + esc(r.uploadedDate) + '</div></div></div><a class="btn btn-sm btn-accent" href="/api/reports/' + r.id + '/download">Download</a></div>').join('') ||
            '<div class="empty-state">No reports uploaded</div>';
    } catch (e) { toast(e.message, 'error'); }
}

async function loadConsultations() {
    try {
        const cons = await api('/api/consultations/patient/' + myPatientId);
        document.getElementById('consultationList').innerHTML = cons.map(c =>
            '<div class="list-item"><div class="li-main"><div><div class="li-title">' + esc(c.diagnosis) + '</div><div class="li-sub">Doctor: ' + esc(c.doctor.name) + ' | ' + esc(c.date) + '</div><div class="li-sub">Details: ' + esc(c.consultationDetails) + '</div><div class="li-sub">Treatment: ' + esc(c.treatment) + '</div><div class="li-sub">Recommendations: ' + esc(c.recommendations) + '</div></div></div></div>').join('') ||
            '<div class="empty-state">No consultation reports</div>';
    } catch (e) { toast(e.message, 'error'); }
}

async function loadBills() {
    try {
        const bills = await api('/api/bills/patient/' + myPatientId);
        document.getElementById('myBillTable').innerHTML = bills.map(b =>
            '<tr><td>' + b.id + '</td><td>' + esc(b.amount) + '</td><td>' + esc(b.date) + '</td><td>' + roleBadge(b.paymentStatus) + '</td><td>' + esc(b.description) + '</td></tr>').join('') ||
            '<tr><td colspan="5" class="empty-state">No bills</td></tr>';
    } catch (e) { toast(e.message, 'error'); }
}

async function searchDoctors() {
    const name = document.getElementById('docNameSearch').value.trim();
    const spec = document.getElementById('docSpecSearch').value.trim();
    let url = '/api/doctors';
    if (name) url += '?name=' + encodeURIComponent(name);
    else if (spec) url += '?specialization=' + encodeURIComponent(spec);
    try {
        const docs = await api(url);
        document.getElementById('doctorResults').innerHTML = docs.map(d =>
            '<div class="doctor-card" style="margin-bottom:14px"><div class="dc-head"><div class="dc-avatar">' + esc(d.name.charAt(0)) + '</div><div><div class="dc-name">' + esc(d.name) + '</div><div class="dc-spec">' + esc(d.specialization) + '</div></div></div>' +
            '<div class="dc-meta">Qualification: ' + esc(d.qualification) + ' · Experience: ' + esc(d.yearsOfExperience) + ' yrs</div>' +
            '<div class="dc-meta">Department: ' + esc((d.department && d.department.name) || 'N/A') + '</div>' +
            '<div class="dc-meta">Rating: <span class="rating">' + esc(d.averageRating) + '</span> (' + esc(d.ratingCount) + ' reviews)</div></div>').join('') ||
            '<div class="empty-state">No doctors found</div>';
    } catch (e) { toast(e.message, 'error'); }
}

async function populateAvailDoctors() {
    try {
        const docs = await api('/api/doctors');
        const sel = document.getElementById('availDocSelect');
        sel.innerHTML = '<option value="">Select Doctor</option>' + docs.map(d => '<option value="' + d.id + '">' + esc(d.name) + ' - ' + esc(d.specialization) + '</option>').join('');
    } catch (e) { toast(e.message, 'error'); }
}

document.getElementById('availDocSelect') && document.getElementById('availDocSelect').addEventListener('change', async () => {
    const id = document.getElementById('availDocSelect').value;
    const info = document.getElementById('availDocInfo');
    if (!id) { info.innerHTML = ''; return; }
    try {
        const doc = await api('/api/doctors/' + id);
        info.innerHTML = '<div class="doctor-card"><div class="dc-head"><div class="dc-avatar">' + esc(doc.name.charAt(0)) + '</div><div><div class="dc-name">' + esc(doc.name) + '</div><div class="dc-spec">' + esc(doc.specialization) + '</div></div></div>' +
            '<div class="dc-meta" style="margin-top:10px"><strong>Availability:</strong></div>' +
            (doc.availability && doc.availability.length ? doc.availability.map(a => '<div class="dc-meta">• ' + esc(a.day) + ': ' + esc(a.startTime) + ' - ' + esc(a.endTime) + '</div>').join('') : '<div class="dc-meta">Not set</div>') +
            '</div>';
    } catch (e) { toast(e.message, 'error'); }
});

async function populateFbDoctors() {
    try {
        const appts = await api('/api/appointments/patient/' + myPatientId);
        const completed = appts.filter(a => a.status === 'COMPLETED');
        const unique = {};
        completed.map(a => a.doctor).forEach(d => unique[d.id] = d);
        document.getElementById('fbDoctor').innerHTML = Object.values(unique).map(d => '<option value="' + d.id + '">' + esc(d.name) + ' (' + esc(d.specialization) + ')</option>').join('') || '<option value="">No completed consultations</option>';
    } catch (e) { toast(e.message, 'error'); }
}

document.addEventListener('DOMContentLoaded', async () => {
    if (!currentUser || currentUser.role !== 'PATIENT') { toast('Please login as Patient', 'error'); setTimeout(logout, 1200); return; }
    initNav();
    await loadStats();
    await loadProfile();
});

document.getElementById('updateProfileBtn') && document.getElementById('updateProfileBtn').addEventListener('click', async () => {
    try {
        await api('/api/patients/' + myPatientId, 'PUT', { name: document.getElementById('uName').value, phone: document.getElementById('uPhone').value });
        toast('Profile updated'); loadStats();
    } catch (e) { toast(e.message, 'error'); }
});

document.getElementById('pcChangeBtn') && document.getElementById('pcChangeBtn').addEventListener('click', async () => {
    const body = {
        currentPassword: document.getElementById('pcCurrent').value,
        newPassword: document.getElementById('pcNew').value,
        confirmPassword: document.getElementById('pcConfirm').value
    };
    try {
        await api('/api/auth/change-password', 'POST', body);
        toast('Password changed successfully', 'success');
        document.getElementById('pcCurrent').value=''; document.getElementById('pcNew').value=''; document.getElementById('pcConfirm').value='';
    } catch (e) { toast(e.message, 'error'); }
});

document.getElementById('docSearchBtn') && document.getElementById('docSearchBtn').addEventListener('click', searchDoctors);

document.getElementById('uploadBtn') && document.getElementById('uploadBtn').addEventListener('click', async () => {
    const title = document.getElementById('upTitle').value.trim();
    const file = document.getElementById('upFile').files[0];
    if (!title) { toast('Enter a title', 'error'); return; }
    const fd = new FormData();
    fd.append('title', title);
    fd.append('reportType', document.getElementById('upType').value);
    fd.append('description', document.getElementById('upDesc').value);
    if (file) fd.append('file', file);
    try {
        const res = await fetch('/api/reports/upload/' + myPatientId, { method: 'POST', body: fd });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Upload failed');
        toast('Report uploaded'); loadReports();
        document.getElementById('upTitle').value=''; document.getElementById('upDesc').value=''; document.getElementById('upFile').value='';
    } catch (e) { toast(e.message, 'error'); }
});

document.getElementById('fbSubmitBtn') && document.getElementById('fbSubmitBtn').addEventListener('click', async () => {
    const docId = document.getElementById('fbDoctor').value;
    if (!docId) { toast('Select a doctor', 'error'); return; }
    const body = { doctor: { id: docId }, rating: parseInt(document.getElementById('fbRating').value), comment: document.getElementById('fbComment').value };
    try {
        await api('/api/feedback', 'POST', body);
        toast('Feedback submitted. Thank you!');
        document.getElementById('fbComment').value='';
    } catch (e) { toast(e.message, 'error'); }
});
