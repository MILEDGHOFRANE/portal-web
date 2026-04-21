// ============================================
// CONFIGURATION SLA — dxc-final
// Aligned with Aroua's SLA model
// ============================================

const API_BASE_URL = 'http://localhost:8086/api';

// ── Preset data (mirrored from Aroua's slaPresets.js) ───────────────────────
const ACCOUNT_PRESETS = {
    'Renault':       { deskAccount: 'Renault Desk',    timeFrame: '40', timeFrameOoh: '40', timeFrameOther: '', answerSla: 'SLA1',        abandonSla: 'Abd1',        otherSla: '', targetAnswerRate: '90%',    targetAbandonRate: '95%',   targetOther: '' },
    'Nissan':        { deskAccount: 'Nissan Desk',     timeFrame: '40', timeFrameOoh: '40', timeFrameOther: '', answerSla: 'SLA1',        abandonSla: 'Abd1',        otherSla: '', targetAnswerRate: '90%',    targetAbandonRate: '95%',   targetOther: '' },
    'Basrah Gas EN': { deskAccount: 'Basrah Gas EN',   timeFrame: '60', timeFrameOoh: '60', timeFrameOther: '', answerSla: 'SLA1',        abandonSla: 'Abd2',        otherSla: '', targetAnswerRate: 'NA',     targetAbandonRate: 'NA',    targetOther: '' },
    'Philips':       { deskAccount: 'Philips Desk',    timeFrame: '60', timeFrameOoh: '60', timeFrameOther: '', answerSla: 'SLA1',        abandonSla: 'Abd2',        otherSla: '', targetAnswerRate: '80%',    targetAbandonRate: '5%',    targetOther: '' },
    'Viatris':       { deskAccount: 'Viatris Desk',    timeFrame: '60', timeFrameOoh: '60', timeFrameOther: '', answerSla: 'SLA1',        abandonSla: 'Abd2',        otherSla: '', targetAnswerRate: '80%',    targetAbandonRate: '5%',    targetOther: '' },
    'XPO':           { deskAccount: 'XPO Desk',        timeFrame: '60', timeFrameOoh: '60', timeFrameOther: '', answerSla: 'SLA1',        abandonSla: 'Abd2',        otherSla: '', targetAnswerRate: '90%',    targetAbandonRate: '3%',    targetOther: '' },
    'Nestle':        { deskAccount: 'Nestle Desk',     timeFrame: '30', timeFrameOoh: '45', timeFrameOther: '90', answerSla: 'SLA1(30sec)', abandonSla: 'SLA1(45sec)', otherSla: 'SLA1(90sec)', targetAnswerRate: '91%', targetAbandonRate: '85%', targetOther: '90%' },
    'Luxottica':     { deskAccount: 'Luxottica Desk',  timeFrame: '30', timeFrameOoh: '30', timeFrameOther: '', answerSla: 'SLA3',        abandonSla: 'Abd5',        otherSla: '', targetAnswerRate: '90%',    targetAbandonRate: '95%',   targetOther: '' },
    'GF':            { deskAccount: 'GF Desk',         timeFrame: '20', timeFrameOoh: '20', timeFrameOther: '', answerSla: 'SLA2',        abandonSla: 'Abd3',        otherSla: '', targetAnswerRate: '80%',    targetAbandonRate: '5%',    targetOther: '' },
    'DXC IT':        { deskAccount: 'DXC IT Desk',     timeFrame: '60', timeFrameOoh: '60', timeFrameOther: '', answerSla: 'SLA2',        abandonSla: 'Abd2',        otherSla: '', targetAnswerRate: '70%',    targetAbandonRate: '5%',    targetOther: '' },
    'HPE':           { deskAccount: 'HPE Desk',        timeFrame: '30', timeFrameOoh: '30', timeFrameOther: '', answerSla: 'SLA2',        abandonSla: 'Abd2',        otherSla: '', targetAnswerRate: '90%',    targetAbandonRate: '3%',    targetOther: '' },
    'Servier':       { deskAccount: 'Servier Desk',    timeFrame: '30', timeFrameOoh: '30', timeFrameOther: '', answerSla: 'SLA1',        abandonSla: 'Abd4',        otherSla: '', targetAnswerRate: '90%',    targetAbandonRate: '2.5%',  targetOther: '' },
    'Sonova':        { deskAccount: 'Sonova Desk',     timeFrame: '60', timeFrameOoh: '60', timeFrameOther: '', answerSla: 'SLA1',        abandonSla: 'Abd4',        otherSla: '', targetAnswerRate: '80%',    targetAbandonRate: '5%',    targetOther: '' },
    'Saipem':        { deskAccount: 'Saipem Desk',     timeFrame: '45', timeFrameOoh: '45', timeFrameOther: '', answerSla: 'SLA2',        abandonSla: 'Abd4',        otherSla: '', targetAnswerRate: '85%',    targetAbandonRate: 'NA',    targetOther: '' },
    'Sony':          { deskAccount: 'Sony Desk',       timeFrame: '30', timeFrameOoh: '30', timeFrameOther: '', answerSla: 'SLA1',        abandonSla: 'Abd2',        otherSla: '', targetAnswerRate: '30 sec', targetAbandonRate: '5%',    targetOther: '' },
};

// ── Constants ─────────────────────────────────────────────────────────────────
const REQUIRED = ['account','deskAccount','timeFrame','timeFrameOoh','answerSla','abandonSla','targetAnswerRate','targetAbandonRate'];
const ALL_FIELDS = ['account','deskAccount','timeFrame','timeFrameOoh','timeFrameOther','answerSla','abandonSla','otherSla','targetAnswerRate','targetAbandonRate','targetOther'];

// ── State ─────────────────────────────────────────────────────────────────────
let state = {
    accountList: [],
    form: emptyForm(),
    errors: {},
    touched: {},
    isExisting: false,
    isSubmitting: false,
};

function emptyForm() {
    return { account:'', deskAccount:'', timeFrame:'', timeFrameOoh:'', timeFrameOther:'', answerSla:'', abandonSla:'', otherSla:'', targetAnswerRate:'', targetAbandonRate:'', targetOther:'' };
}

// ── Auth ──────────────────────────────────────────────────────────────────────
function checkAuth() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if (!user.id) { window.location.href = '../html/login.html'; return null; }
    const name = `${user.firstName || ''} ${user.lastName || ''}`.trim();
    const initials = ((user.firstName||'?')[0] + (user.lastName||'')[0]).toUpperCase();
    const el = document.getElementById('sidebarAvatar');
    if (el) el.textContent = initials;
    const sn = document.getElementById('sidebarName');
    if (sn) sn.textContent = name;
    const hu = document.getElementById('headerUser');
    if (hu) hu.textContent = name;
    return user;
}

function logout() { localStorage.clear(); window.location.href = '../html/login.html'; }

function getAuthHeaders() {
    const token = localStorage.getItem('token');
    return token ? { Authorization: `Bearer ${token}` } : {};
}

// ── Validation ────────────────────────────────────────────────────────────────
function isSpecial(v) { const c = String(v).trim().toLowerCase(); return c==='na'||c==='n/a'||c==='-'; }

function isTargetValid(v) {
    const c = String(v).trim();
    if (!c) return false;
    if (isSpecial(c)) return true;
    if (/^\d+(\.\d+)?\s*(s|sec|secs|second|seconds)$/i.test(c)) return true;
    if (/^\d+(\.\d+)?%$/.test(c)) { const n=Number(c.replace('%','')); return n>=0&&n<=100; }
    if (/^\d+(\.\d+)?$/.test(c)) { const n=Number(c); return n>=0&&n<=100; }
    return false;
}

function validate(form) {
    const e = {};
    REQUIRED.forEach(f => { if (!String(form[f]??'').trim()) e[f]='Ce champ est requis.'; });
    if (String(form.timeFrame).trim()     && !/^\d+$/.test(String(form.timeFrame).trim()))     e.timeFrame='Doit être un entier positif.';
    if (String(form.timeFrameOoh).trim()  && !/^\d+$/.test(String(form.timeFrameOoh).trim()))  e.timeFrameOoh='Doit être un entier positif.';
    if (String(form.timeFrameOther).trim()&& !/^\d+$/.test(String(form.timeFrameOther).trim())) e.timeFrameOther='Doit être un entier positif.';
    if (String(form.targetAnswerRate).trim() && !isTargetValid(form.targetAnswerRate))  e.targetAnswerRate='Valeur: 0-100, %, sec, NA ou -.';
    if (String(form.targetAbandonRate).trim()&& !isTargetValid(form.targetAbandonRate)) e.targetAbandonRate='Valeur: 0-100, %, sec, NA ou -.';
    if (String(form.targetOther).trim()   && !isTargetValid(form.targetOther))          e.targetOther='Valeur: 0-100, %, sec, NA ou -.';
    if (String(form.otherSla).trim()&&!isSpecial(form.otherSla)&&!String(form.timeFrameOther).trim()) e.timeFrameOther='Requis quand Autre SLA est renseigné.';
    if (String(form.otherSla).trim()&&!isSpecial(form.otherSla)&&!String(form.targetOther).trim())    e.targetOther='Requis quand Autre SLA est renseigné.';
    return e;
}

// ── Field mapping to DOM ids ──────────────────────────────────────────────────
const FIELD_IDS = {
    account:'fieldAccount', deskAccount:'fieldDeskAccount', timeFrame:'fieldTimeFrame',
    timeFrameOoh:'fieldTimeFrameOoh', timeFrameOther:'fieldTimeFrameOther',
    answerSla:'fieldAnswerSla', abandonSla:'fieldAbandonSla', otherSla:'fieldOtherSla',
    targetAnswerRate:'fieldTargetAnswerRate', targetAbandonRate:'fieldTargetAbandonRate', targetOther:'fieldTargetOther',
};

// ── State mutators ────────────────────────────────────────────────────────────
function setField(name, value) {
    state.form[name] = value;
    state.errors = validate(state.form);
    renderErrors();
    renderUnitBtns();
    clearNotif();
}

function touchField(name) {
    state.touched[name] = true;
    renderErrors();
}

function touchAll() { ALL_FIELDS.forEach(f => { state.touched[f] = true; }); }

// ── Render ────────────────────────────────────────────────────────────────────
function renderErrors() {
    ALL_FIELDS.forEach(f => {
        const errEl = document.getElementById(`error-${f}`);
        const inputEl = document.getElementById(FIELD_IDS[f]);
        if (!errEl) return;
        const show = !!state.errors[f] && !!state.touched[f];
        errEl.textContent = show ? state.errors[f] : '';
        errEl.className = 'field-error' + (show ? ' visible' : '');
        if (inputEl) inputEl.classList.toggle('is-error', show);
    });
}

function renderFormTitle() {
    const t = document.getElementById('formTitle');
    const l = document.getElementById('submitLabel');
    const d = document.getElementById('deleteBtn');
    if (state.isExisting) {
        t.textContent = `Modification : ${state.form.account}`;
        l.textContent = 'Mettre à jour';
        d.disabled = false;
    } else {
        t.textContent = 'Nouvelle configuration';
        l.textContent = 'Ajouter';
        d.disabled = true;
    }
}

function renderAccountField() {
    const wrapper = document.getElementById('accountFieldWrapper');
    if (state.isExisting) {
        const opts = state.accountList.map(a => `<option value="${esc(a)}"${a===state.form.account?' selected':''}>${esc(a)}</option>`).join('');
        wrapper.innerHTML = `<select id="fieldAccount" class="form-control" onchange="handleAccountChange(this.value)" onblur="touchField('account')"><option value="">— sélectionner —</option>${opts}</select>`;
    } else {
        wrapper.innerHTML = `<input type="text" id="fieldAccount" class="form-control" placeholder="Nom de l'account" value="${esc(state.form.account)}" oninput="setField('account',this.value)" onblur="touchField('account')">`;
    }
}

function syncInputs() {
    Object.entries(FIELD_IDS).forEach(([key, id]) => {
        const el = document.getElementById(id);
        if (el) el.value = state.form[key] || '';
    });
}

function renderToolbarSelect() {
    const sel = document.getElementById('toolbarSelect');
    sel.innerHTML = '<option value="">Sélectionner un account...</option>' +
        state.accountList.map(a => `<option value="${esc(a)}"${a===state.form.account?' selected':''}>${esc(a)}</option>`).join('');
}

function renderUnitBtns() {
    setUnitActive('ansBtn-pct','ansBtn-sec', state.form.targetAnswerRate);
    setUnitActive('abdBtn-pct','abdBtn-sec', state.form.targetAbandonRate);
}

function setUnitActive(pctId, secId, val) {
    const v = String(val??'').trim();
    const p = document.getElementById(pctId);
    const s = document.getElementById(secId);
    if (p) p.classList.toggle('active', /^\d+(\.\d+)?%$/.test(v));
    if (s) s.classList.toggle('active', /^\d+(\.\d+)?\s*(s|sec|secs|second|seconds)$/i.test(v));
}

function esc(str) { return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }

// ── Notification ──────────────────────────────────────────────────────────────
function showNotif(msg, type) {
    const el = document.getElementById('notif');
    const m  = document.getElementById('notifMsg');
    const a  = document.getElementById('notifActions');
    el.className = `notif notif-${type} visible`;
    m.textContent = msg;
    a.style.display = type === 'warning' ? 'flex' : 'none';
}

function clearNotif() {
    const el = document.getElementById('notif');
    el.className = 'notif';
}

// ── Unit toggle ───────────────────────────────────────────────────────────────
function applyUnit(field, unit) {
    const cur = String(state.form[field]??'').trim();
    const num = (cur.match(/\d+(\.\d+)?/) || ['0'])[0];
    state.form[field] = unit === 'percent' ? `${num}%` : `${num} sec`;
    state.touched[field] = true;
    state.errors = validate(state.form);
    syncInputs();
    renderErrors();
    renderUnitBtns();
}

// ── API ───────────────────────────────────────────────────────────────────────
async function loadAllAccounts() {
    try {
        const res  = await fetch(`${API_BASE_URL}/sla/all`, { headers: getAuthHeaders() });
        const data = await res.json();
        state.accountList = (data.success && Array.isArray(data.data)) ? data.data.map(c => c.account) : [];
    } catch { state.accountList = []; }
    renderToolbarSelect();
}

async function loadByAccount(account) {
    try {
        const res  = await fetch(`${API_BASE_URL}/sla/${encodeURIComponent(account)}`, { headers: getAuthHeaders() });
        if (res.status === 404) return null;
        const data = await res.json();
        if (data.success && data.data) {
            const c = data.data;
            return { account: c.account||'', deskAccount: c.deskAccount||'', timeFrame: c.timeFrame||'', timeFrameOoh: c.timeFrameOoh||'', timeFrameOther: c.timeFrameOther||'', answerSla: c.answerSla||'', abandonSla: c.abandonSla||'', otherSla: c.otherSla||'', targetAnswerRate: c.targetAnswerRate||'', targetAbandonRate: c.targetAbandonRate||'', targetOther: c.targetOther||'' };
        }
    } catch {}
    return null;
}

// ── Handlers ──────────────────────────────────────────────────────────────────
function handleNew() {
    state.form = emptyForm(); state.errors = {}; state.touched = {}; state.isExisting = false;
    document.getElementById('toolbarSelect').value = '';
    renderAccountField(); syncInputs(); renderFormTitle(); renderErrors(); renderUnitBtns(); clearNotif();
}

async function handleToolbarSelect(account) {
    if (!account) { handleNew(); return; }
    const config = await loadByAccount(account);
    if (config) { state.form = config; state.isExisting = true; }
    else { state.form = emptyForm(); state.isExisting = false; }
    state.errors = validate(state.form); state.touched = {};
    renderAccountField(); syncInputs(); renderFormTitle(); renderErrors(); renderUnitBtns(); clearNotif();
}

async function handleAccountChange(account) {
    if (!account || !state.isExisting) return;
    const config = await loadByAccount(account);
    if (config) {
        state.form = config; state.errors = validate(state.form); state.touched = {};
        syncInputs(); renderFormTitle(); renderErrors(); renderUnitBtns();
        document.getElementById('toolbarSelect').value = account;
    }
}

function handleReset() {
    state.form = emptyForm(); state.errors = {}; state.touched = {}; state.isExisting = false;
    document.getElementById('toolbarSelect').value = '';
    renderAccountField(); syncInputs(); renderFormTitle(); renderErrors(); renderUnitBtns(); clearNotif();
}

function handleDelete() {
    if (!state.isExisting || !state.form.account) return;
    showNotif(`Voulez-vous vraiment supprimer "${state.form.account}" ?`, 'warning');
}

async function confirmDelete() {
    try {
        const res  = await fetch(`${API_BASE_URL}/sla/${encodeURIComponent(state.form.account)}`, { method: 'DELETE', headers: getAuthHeaders() });
        const data = await res.json();
        if (data.success) { await loadAllAccounts(); handleReset(); showNotif('Configuration supprimée avec succès.', 'success'); }
        else showNotif(data.message || 'Erreur lors de la suppression.', 'error');
    } catch { showNotif('Impossible de joindre le serveur.', 'error'); }
}

async function handleSubmit(event) {
    event.preventDefault();
    touchAll();
    state.errors = validate(state.form);
    renderErrors();
    if (Object.keys(state.errors).length > 0) { showNotif('Veuillez corriger les erreurs avant de soumettre.', 'error'); return; }

    state.isSubmitting = true;
    document.getElementById('submitBtn').disabled = true;

    try {
        const isUpdate = state.isExisting;
        const url    = isUpdate ? `${API_BASE_URL}/sla/${encodeURIComponent(state.form.account)}` : `${API_BASE_URL}/sla/save`;
        const method = isUpdate ? 'PUT' : 'POST';
        const res    = await fetch(url, { method, headers: { 'Content-Type':'application/json', ...getAuthHeaders() }, body: JSON.stringify(state.form) });
        const data   = await res.json();

        if (data.success) {
            if (!isUpdate) state.isExisting = true;
            await loadAllAccounts();
            document.getElementById('toolbarSelect').value = state.form.account;
            renderFormTitle();
            showNotif(data.message || (isUpdate ? 'Configuration mise à jour !' : 'Configuration créée !'), 'success');
        } else {
            showNotif(data.message || 'Erreur lors de la sauvegarde.', 'error');
        }
    } catch { showNotif('Impossible de joindre le serveur.', 'error'); }
    finally { state.isSubmitting = false; document.getElementById('submitBtn').disabled = false; }
}

// ── Init ──────────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
    checkAuth();
    await loadAllAccounts();
    renderAccountField();
    renderFormTitle();
    renderErrors();
    renderUnitBtns();
    console.log('✅ Config SLA prête');
});
