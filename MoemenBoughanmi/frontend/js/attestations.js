const API_URL = 'http://localhost:8085/api/attestations';
const token = localStorage.getItem('authToken');
const user = JSON.parse(localStorage.getItem('user') || '{}');

if (!token) window.location.href = '../html/login.html';

async function loadAttestations() {
    const res = await fetch(API_URL + '/mes-demandes', { headers: { 'Authorization': 'Bearer ' + token } });
    const data = await res.json();
    const list = document.getElementById('attestationsList');
    if (data.success && data.attestations.length > 0) {
        list.innerHTML = data.attestations.map(a => `
            <div class="dxc-card" style="margin-bottom: 15px; padding: 20px;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <h3 style="color: var(--dxc-primary); margin-bottom: 10px;">Attestation de ${a.type}</h3>
                        <div style="color: #666; font-size: 14px;">Demandée le ${new Date(a.dateCreation).toLocaleDateString('fr-FR')}</div>
                        ${a.numeroAttestation ? '<div style="margin-top: 5px; font-weight: 600;">N° ' + a.numeroAttestation + '</div>' : ''}
                    </div>
                    <div>
                        <span class="dxc-badge dxc-badge-${a.statut === 'GENEREE' ? 'success' : 'warning'}">${a.statut === 'GENEREE' ? 'Disponible' : 'En attente'}</span>
                        ${a.statut === 'GENEREE' ? '<button class="dxc-btn dxc-btn-sm dxc-btn-primary" style="margin-left: 10px;">Télécharger</button>' : ''}
                    </div>
                </div>
            </div>
        `).join('');
    } else {
        list.innerHTML = '<div class="dxc-card">Aucune attestation demandée</div>';
    }
}

async function demanderType(type) {
    const motif = prompt('Motif de la demande (optionnel):');
    const body = {
        type: type,
        motif: motif || '',
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
        loadAttestations();
    }
}

function logout() {
    localStorage.clear();
    window.location.href = '../html/login.html';
}

loadAttestations();
