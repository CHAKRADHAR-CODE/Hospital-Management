/* ============ DOCTOR DASHBOARD ============ */

let myDoctorId = null;
let assignedPatients = [];

function initNav() {
    document.querySelectorAll('.sidebar .nav-item[data-panel]').forEach(item => {
        item.addEventListener('click', () => {
            document.querySelectorAll('.sidebar .nav-item[data-panel]').forEach(i => i.classList.remove('active'));
            document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
            item.classList.add('active');
            const panel = document.getElementById('panel-' + item.dataset.panel);
            if (panel) panel.classList.add('active');
            const handlers = {
                appointments: loadMyAppointments,
                patients: loadAssignedPatients,
                availability: renderAvail,
                profile: loadProfile,
                reports: populateReportPatients,
                prescriptions: populateRxPatients,
                consultations: populateConPatients,
                feedback: loadFeedback
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

async function loadStats() {
    try {
        const me = await api('/api/doctors/me');
        myDoctorId = me.id;
        let appts = await api('/api/appointments/doctor/' + me.id);
        const today = new Date().toISOString().slice(0, 10);
        const todayAppts = appts.filter(a => a.date === today);
        document.getElementById('statGrid').innerHTML =
            '<div class="stat"><div class="stat-icon">📅</div><div class="stat-value">' + appts.length + '</div><div class="stat-label">Total Appointments</div></div>' +
            '<div class="stat"><div class="stat-icon">⏳</div><div class="stat-value">' + todayAppts.length + '</div><div class="stat-label">Today</div></div>' +
            '<div class="stat"><div class="stat-icon">⭐</div><div class="stat-value">' + (me.averageRating || 0) + '</div><div class="stat-label">Rating (' + (me.ratingCount || 0) + ')</div></div>';
        document.getElementById('todayAppts').innerHTML = todayAppts.length ?
            todayAppts.map(a => '<div class="list-item"><div class="li-main"><div class="li-title">' + esc(a.patient.name) + '</div><div class="li-sub">' + esc(a.time) + ' - ' + esc(a.reason || 'General') + '</div></div>' + roleBadge(a.status) + '</div>').join('')
            : '<div class="empty-state">No appointments today</div>';
    } catch (e) { toast(e.message, 'error'); }
}

async function loadMyAppointments() {
    try {
        const appts = await api('/api/appointments/doctor/' + myDoctorId);
        document.getElementById('docApptTable').innerHTML = appts.map(a =>
            '<tr><td>' + a.id + '</td><td>' + esc(a.patient.name) + '</td><td>' + esc(a.date) + '</td><td>' + esc(a.time) + '</td><td>' + roleBadge(a.status) + '</td><td>' + esc(a.reason) + '</td><td>' +
            (a.status === 'SCHEDULED' ? '<button class="btn btn-sm btn-success" data-complete="' + a.id + '">Complete</button>' : '') +
            '</td></tr>').join('') || '<tr><td colspan="7" class="empty-state">No appointments</td></tr>';
        document.querySelectorAll('[data-complete]').forEach(b => b.addEventListener('click', async () => {
            try { await api('/api/appointments/' + b.dataset.complete + '/complete', 'PUT'); toast('Appointment completed'); loadMyAppointments(); } catch (e) { toast(e.message, 'error'); }
        }));
    } catch (e) { toast(e.message, 'error'); }
}

async function loadAssignedPatients() {
    try {
        const appts = await api('/api/appointments/doctor/' + myDoctorId);
        const seen = {};
        appts.map(a => a.patient).forEach(p => { seen[p.id] = p; });
        assignedPatients = Object.values(seen);
        document.getElementById('assignedPatients').innerHTML = assignedPatients.map(p =>
            '<tr><td>' + p.id + '</td><td>' + esc(p.name) + '</td><td>' + esc(p.age) + '</td><td>' + esc(p.gender) + '</td><td>' + esc(p.bloodGroup) + '</td><td>' + esc(p.phone) + '</td></tr>').join('') ||
            '<tr><td colspan="6" class="empty-state">No patients assigned</td></tr>';
        populateRxPatients(); populateConPatients(); populateReportPatients();
    } catch (e) { toast(e.message, 'error'); }
}

function patientOptions() {
    return assignedPatients.map(p => '<option value="' + p.id + '">' + esc(p.name) + ' (ID ' + p.id + ')</option>').join('');
}

async function populateRxPatients() {
    document.getElementById('rxPatient').innerHTML = patientOptions() || '<option value="">No patients</option>';
    const me = await api('/api/doctors/me');
    const rxs = await api('/api/prescriptions/doctor/' + me.id);
    document.getElementById('rxList').innerHTML = rxs.map(r =>
        '<div class="list-item"><div class="li-main"><div class="li-title">' + esc(r.medicine) + ' · ' + esc(r.patient.name) + '</div><div class="li-sub">' + esc(r.dosage) + ' | ' + esc(r.frequency) + ' | ' + esc(r.duration) + ' | ' + esc(r.date) + '</div></div></div>').join('') ||
        '<div class="empty-state">No prescriptions yet</div>';
}

async function populateConPatients() {
    document.getElementById('conPatient').innerHTML = patientOptions() || '<option value="">No patients</option>';
    const me = await api('/api/doctors/me');
    const cons = await api('/api/consultations/doctor/' + me.id);
    document.getElementById('conList').innerHTML = cons.map(c =>
        '<div class="list-item"><div class="li-main"><div><div class="li-title">' + esc(c.patient.name) + ' - ' + esc(c.diagnosis) + '</div><div class="li-sub">' + esc(c.date) + ' · Treated: ' + esc(c.treatment || 'N/A') + '</div></div></div></div>').join('') ||
        '<div class="empty-state">No consultation reports</div>';
}

async function populateReportPatients() {
    document.getElementById('reportPatient').innerHTML = patientOptions() || '<option value="">No patients</option>';
}

document.getElementById('reportPatient') && (async () => {
    const sel = document.getElementById('reportPatient');
    sel.addEventListener('change', async () => {
        const pid = sel.value;
        const list = document.getElementById('reportList');
        if (!pid) { list.innerHTML = ''; return; }
        try {
            const reports = await api('/api/reports/patient/' + pid);
            list.innerHTML = reports.map(r =>
                '<div class="list-item"><div class="li-main"><div class="li-title">' + esc(r.title) + '</div><div class="li-sub">' + esc(r.reportType || 'Report') + ' · ' + esc(r.uploadedDate) + '</div></div>' +
                (r.fileData || r.id ? '<a class="btn btn-sm btn-accent" href="/api/reports/' + r.id + '/download">Download</a>' : '') + '</div>').join('') ||
                '<div class="empty-state">No reports for this patient</div>';
        } catch (e) { toast(e.message, 'error'); }
    });
})();

async function loadProfile() {
    try {
        const me = await api('/api/doctors/me');
        document.getElementById('profileDetail').innerHTML =
            '<div class="doctor-card"><div class="dc-head"><div class="dc-avatar">' + esc(me.name.charAt(0)) + '</div><div><div class="dc-name">' + esc(me.name) + '</div><div class="dc-spec">' + esc(me.specialization) + '</div></div></div>' +
            '<div class="dc-meta">Qualification: ' + esc(me.qualification || 'N/A') + '</div>' +
            '<div class="dc-meta">Experience: ' + esc(me.yearsOfExperience) + ' years</div>' +
            '<div class="dc-meta">Department: ' + esc((me.department && me.department.name) || 'N/A') + '</div>' +
            '<div class="dc-meta">Phone: ' + esc(me.phone) + '</div>' +
            '<div class="dc-meta">Email: ' + esc(me.email) + '</div>' +
            '<div class="dc-meta">Rating: <span class="rating">' + esc(me.averageRating) + '</span> (' + esc(me.ratingCount) + ' reviews)</div>' +
            '<div class="dc-meta" style="margin-top:8px">Bio: ' + esc(me.bio || '') + '</div></div>';
    } catch (e) { toast(e.message, 'error'); }
}

document.getElementById('dcChangeBtn') && document.getElementById('dcChangeBtn').addEventListener('click', async () => {
    const body = {
        currentPassword: document.getElementById('dcCurrent').value,
        newPassword: document.getElementById('dcNew').value,
        confirmPassword: document.getElementById('dcConfirm').value
    };
    try {
        await api('/api/auth/change-password', 'POST', body);
        toast('Password changed successfully', 'success');
        document.getElementById('dcCurrent').value=''; document.getElementById('dcNew').value=''; document.getElementById('dcConfirm').value='';
    } catch (e) { toast(e.message, 'error'); }
});

function renderAvail() {
    const list = document.getElementById('availList');
    try {
        api('/api/doctors/me').then(me => {
            myDoctorId = me.id;
            list.innerHTML = (me.availability && me.availability.length ? me.availability.map((a, i) =>
                '<div class="list-item"><div class="li-main"><div class="li-title">' + esc(a.day) + '</div><div class="li-sub">' + esc(a.startTime) + ' - ' + esc(a.endTime) + '</div></div><button class="btn btn-sm btn-danger" data-remove-slot="' + i + '">Remove</button></div>').join('')
                : '<div class="empty-state">No availability set</div>');
            document.querySelectorAll('[data-remove-slot]').forEach(b => b.addEventListener('click', (e) => {
                e.preventDefault();
                // handled in saveAvail flow
            }));
        }).catch(e => toast(e.message, 'error'));
    } catch (e) {}
}

function collectAvail() {
    return Array.from(document.querySelectorAll('#availList .list-item')).map(item => {
        const title = item.querySelector('.li-title').textContent;
        const sub = item.querySelector('.li-sub').textContent.split(' - ');
        return { day: title, startTime: sub[0], endTime: sub[1] };
    });
}

document.getElementById('addAvailSlot') && document.getElementById('addAvailSlot').addEventListener('click', () => {
    const day = document.getElementById('avDay').value;
    const start = document.getElementById('avStart').value;
    const end = document.getElementById('avEnd').value;
    if (!start || !end) { toast('Select start and end time', 'error'); return; }
    if (start >= end) { toast('End time must be after start time', 'error'); return; }
    const list = document.getElementById('availList');
    if (list.querySelector('.empty-state')) list.innerHTML = '';
    list.innerHTML += '<div class="list-item"><div class="li-main"><div class="li-title">' + esc(day) + '</div><div class="li-sub">' + esc(start) + ' - ' + esc(end) + '</div></div><button class="btn btn-sm btn-danger remove-slot-btn">Remove</button></div>';
    list.querySelectorAll('.remove-slot-btn').forEach(b => b.addEventListener('click', () => b.closest('.list-item').remove()));
});

document.getElementById('clearAvail') && document.getElementById('clearAvail').addEventListener('click', () => {
    document.getElementById('availList').innerHTML = '<div class="empty-state">No availability set</div>';
});

document.getElementById('saveAvail') && document.getElementById('saveAvail').addEventListener('click', async () => {
    const existing = document.getElementById('availList').querySelector('.empty-state');
    let slots = [];
    if (!existing) slots = collectAvail();
    // convert HH:MM to AM/PM for display consistency
    const conv = slots.map(s => ({
        day: s.day,
        startTime: toAmPm(s.startTime),
        endTime: toAmPm(s.endTime)
    }));
    try {
        await api('/api/doctors/' + myDoctorId + '/availability', 'PUT', conv);
        toast('Availability saved'); renderAvail();
    } catch (e) { toast(e.message, 'error'); }
});

function toAmPm(t) {
    if (!t) return '';
    let [h, m] = t.split(':');
    h = parseInt(h); let suffix = h >= 12 ? 'PM' : 'AM';
    let h12 = h % 12; if (h12 === 0) h12 = 12;
    return h12 + ':' + m + ' ' + suffix;
}

async function loadFeedback() {
    try {
        const me = await api('/api/doctors/me');
        const fb = await api('/api/feedback/doctor/' + me.id);
        document.getElementById('feedbackList').innerHTML = fb.map(f =>
            '<div class="list-item"><div class="li-main"><div><div class="li-title stars">' + '★'.repeat(f.rating) + '<span style="color:var(--text-muted)">' + '★'.repeat(5 - f.rating) + '</span></div><div class="li-sub">' + esc(f.patient.name) + '</div><div class="li-sub">' + esc(f.comment || '') + '</div></div></div></div>').join('') ||
            '<div class="empty-state">No feedback yet</div>';
    } catch (e) { toast(e.message, 'error'); }
}

document.addEventListener('DOMContentLoaded', async () => {
    if (!currentUser || currentUser.role !== 'DOCTOR') { toast('Please login as Doctor', 'error'); setTimeout(logout, 1200); return; }
    document.getElementById('docName').textContent = currentUser.username || 'Doctor';
    initNav();
    await loadStats();
    renderAvail();
});

document.getElementById('addRxBtn') && document.getElementById('addRxBtn').addEventListener('click', async () => {
    const pid = document.getElementById('rxPatient').value;
    if (!pid) { toast('Select a patient', 'error'); return; }
    const body = {
        medicine: document.getElementById('rxMedicine').value.trim(),
        dosage: document.getElementById('rxDosage').value.trim(),
        frequency: document.getElementById('rxFrequency').value.trim(),
        duration: document.getElementById('rxDuration').value.trim(),
        instructions: document.getElementById('rxInstructions').value.trim()
    };
    try {
        await api('/api/prescriptions/patient/' + pid, 'POST', body);
        toast('Prescription added'); populateRxPatients();
        document.getElementById('rxMedicine').value=''; document.getElementById('rxDosage').value='';
        document.getElementById('rxFrequency').value=''; document.getElementById('rxDuration').value='';
        document.getElementById('rxInstructions').value='';
    } catch (e) { toast(e.message, 'error'); }
});

document.getElementById('addConBtn') && document.getElementById('addConBtn').addEventListener('click', async () => {
    const pid = document.getElementById('conPatient').value;
    if (!pid) { toast('Select a patient', 'error'); return; }
    const body = {
        diagnosis: document.getElementById('conDiagnosis').value.trim(),
        consultationDetails: document.getElementById('conDetails').value.trim(),
        treatment: document.getElementById('conTreatment').value.trim(),
        recommendations: document.getElementById('conRecommendations').value.trim()
    };
    try {
        await api('/api/consultations/patient/' + pid, 'POST', body);
        toast('Consultation report added'); populateConPatients();
        document.getElementById('conDiagnosis').value=''; document.getElementById('conDetails').value='';
        document.getElementById('conTreatment').value=''; document.getElementById('conRecommendations').value='';
    } catch (e) { toast(e.message, 'error'); }
});
