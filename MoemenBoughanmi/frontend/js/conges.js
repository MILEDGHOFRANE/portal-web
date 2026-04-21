const API_URL = 'http://localhost:8085/api/conges';
const token = localStorage.getItem('authToken');
const user = JSON.parse(localStorage.getItem('user') || '{}');

if (!token) window.location.href = '../html/login.html';

async function loadSolde() {
    const res = await fetch(API_URL + '/solde', { headers: { 'Authorization': 'Bearer ' + token } });
    const data = await res.json();
    if (data.success) {
        document.getElementById('congesTotal').textContent = data.solde.total;
        document.getElementById('congesUtilises').textContent = data.solde.utilises;
        document.getElementById('congesRestants').textContent = data.solde.restants;
    }
}

async function loadConges() {
    const res = await fetch(API_URL + '/mes-demandes', { headers: { 'Authorization': 'Bearer ' + token } });
    const data = await res.json();
    const list = document.getElementById('congesList');
    if (data.success && data.conges.length > 0) {
        list.innerHTML = data.conges.map(c => `
            <div class="conge-card">
                <div class="conge-header">
                    <div class="conge-type">${c.type.replace('_', ' ')}</div>
                    <span class="conge-status status-${c.statut.toLowerCase().replace('_', '-')}">${c.statut.replace('_', ' ')}</span>
                </div>
                <div class="conge-dates">
                    <div class="conge-info">📅 Du ${new Date(c.dateDebut).toLocaleDateString('fr-FR')}</div>
                    <div class="conge-info">au ${new Date(c.dateFin).toLocaleDateString('fr-FR')}</div>
                    <div class="conge-info">(${c.nombreJours} jours)</div>
                </div>
                ${c.motif ? '<div class="conge-info">💬 ' + c.motif + '</div>' : ''}
                ${c.motifRejet ? '<div style="color:#721C24; margin-top:10px;">❌ Rejeté: ' + c.motifRejet + '</div>' : ''}
            </div>
        `).join('');
    } else {
        list.innerHTML = '<div class="dxc-card">Aucune demande de congé</div>';
    }
}

function showDemandeModal() {
    document.getElementById('demandeModal').style.display = 'block';
}

function hideDemandeModal() {
    document.getElementById('demandeModal').style.display = 'none';
}

document.getElementById('demandeForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const body = {
        type: document.getElementById('typeConge').value,
        dateDebut: document.getElementById('dateDebut').value,
        dateFin: document.getElementById('dateFin').value,
        motif: document.getElementById('motif').value,
        userEmail: user.email,
        userName: user.firstName + ' ' + user.lastName
    };
    const res = await fetch(API_URL + '/demander', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify(body)
    });
    const data = await res.json();
    if (data.success) {
        alert('Demande envoyée !');
        hideDemandeModal();
        loadConges();
        loadSolde();
    }
});

function logout() {
    localStorage.clear();
    window.location.href = '../html/login.html';
}

loadSolde();
loadConges();