const API_BASE_URL_CONGES = 'http://localhost:8085/api/conges';
const API_BASE_URL_ATTESTATIONS = 'http://localhost:8085/api/attestations';
const token = localStorage.getItem('authToken');
const user = JSON.parse(localStorage.getItem('user') || '{}');

if (!token) {
    window.location.href = '../html/login.html';
}

if (user && user.firstName) {
    document.getElementById('userName').textContent = user.firstName;
}

async function loadStats() {
    try {
        const [congesRes, attestationsRes] = await Promise.all([
            fetch(API_BASE_URL_CONGES + '/toutes', { headers: { 'Authorization': 'Bearer ' + token } }).catch(() => ({ ok: false })),
            fetch(API_BASE_URL_ATTESTATIONS + '/toutes', { headers: { 'Authorization': 'Bearer ' + token } }).catch(() => ({ ok: false }))
        ]);

        let totalConges = 0, totalAttestations = 0, enAttente = 0, approuvees = 0;

        if (congesRes.ok) {
            const congesData = await congesRes.json();
            if (congesData.success && congesData.conges) {
                totalConges = congesData.conges.length;
                enAttente += congesData.conges.filter(c => c.statut === 'EN_ATTENTE').length;
                approuvees += congesData.conges.filter(c => c.statut === 'APPROUVE').length;
            }
        }

        if (attestationsRes.ok) {
            const attestationsData = await attestationsRes.json();
            if (attestationsData.success && attestationsData.attestations) {
                totalAttestations = attestationsData.attestations.length;
                enAttente += attestationsData.attestations.filter(a => a.statut === 'EN_ATTENTE').length;
                approuvees += attestationsData.attestations.filter(a => a.statut === 'GENEREE').length;
            }
        }

        document.getElementById('totalConges').textContent = totalConges;
        document.getElementById('totalAttestations').textContent = totalAttestations;
        document.getElementById('enAttente').textContent = enAttente;
        document.getElementById('approuvees').textContent = approuvees;

    } catch (error) {
        console.log('Mode demo - stats simulées');
        document.getElementById('totalConges').textContent = '12';
        document.getElementById('totalAttestations').textContent = '8';
        document.getElementById('enAttente').textContent = '5';
        document.getElementById('approuvees').textContent = '15';
    }
}

async function loadDemandes() {
    try {
        const [congesRes, attestationsRes] = await Promise.all([
            fetch(API_BASE_URL_CONGES + '/toutes', { headers: { 'Authorization': 'Bearer ' + token } }).catch(() => ({ ok: false })),
            fetch(API_BASE_URL_ATTESTATIONS + '/toutes', { headers: { 'Authorization': 'Bearer ' + token } }).catch(() => ({ ok: false }))
        ]);

        let demandes = [];

        if (congesRes.ok) {
            const congesData = await congesRes.json();
            if (congesData.success && congesData.conges) {
                demandes = demandes.concat(congesData.conges.map(c => ({
                    id: c.id,
                    type: 'Congé',
                    typeDetail: c.type,
                    userName: c.userName,
                    date: c.dateCreation,
                    statut: c.statut,
                    source: 'conges'
                })));
            }
        }

        if (attestationsRes.ok) {
            const attestationsData = await attestationsRes.json();
            if (attestationsData.success && attestationsData.attestations) {
                demandes = demandes.concat(attestationsData.attestations.map(a => ({
                    id: a.id,
                    type: 'Attestation',
                    typeDetail: a.type,
                    userName: a.userName,
                    date: a.dateCreation,
                    statut: a.statut,
                    source: 'attestations'
                })));
            }
        }

        demandes.sort((a, b) => new Date(b.date) - new Date(a.date));
        demandes = demandes.slice(0, 10);

        const tbody = document.getElementById('demandesTableBody');
        
        if (demandes.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align: center; padding: 40px; color: var(--dxc-gray-600);">
                        <div style="font-size: 48px; margin-bottom: 16px;">📭</div>
                        <div>Aucune demande pour le moment</div>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = demandes.map(d => {
            const statusClass = d.statut === 'EN_ATTENTE' ? 'pending' : d.statut === 'APPROUVE' || d.statut === 'GENEREE' ? 'approved' : 'rejected';
            const statusText = d.statut === 'EN_ATTENTE' ? 'En Attente' : d.statut === 'APPROUVE' || d.statut === 'GENEREE' ? 'Approuvée' : 'Rejetée';
            
            return `
                <tr>
                    <td><strong>${d.userName}</strong></td>
                    <td>${d.type} <span style="color: var(--dxc-gray-600);">(${d.typeDetail.replace('_', ' ')})</span></td>
                    <td>${new Date(d.date).toLocaleDateString('fr-FR')}</td>
                    <td><span class="status-badge status-${statusClass}">${statusText}</span></td>
                    <td>
                        ${d.statut === 'EN_ATTENTE' ? `
                            <button class="btn-icon btn-approve" onclick="approuver('${d.source}', ${d.id})">✓ Approuver</button>
                            <button class="btn-icon btn-reject" onclick="rejeter('${d.source}', ${d.id})">✗ Rejeter</button>
                        ` : `
                            <button class="btn-icon" style="background: var(--dxc-accent); color: white;" onclick="window.location.href='${d.source}.html'">Voir</button>
                        `}
                    </td>
                </tr>
            `;
        }).join('');

    } catch (error) {
        console.log('Mode demo - demandes simulées');
        const tbody = document.getElementById('demandesTableBody');
        tbody.innerHTML = `
            <tr>
                <td><strong>Marie Leroy</strong></td>
                <td>Congé <span style="color: var(--dxc-gray-600);">(Annuel)</span></td>
                <td>${new Date().toLocaleDateString('fr-FR')}</td>
                <td><span class="status-badge status-pending">En Attente</span></td>
                <td>
                    <button class="btn-icon btn-approve" onclick="alert('Approuver Marie Leroy')">✓ Approuver</button>
                    <button class="btn-icon btn-reject" onclick="alert('Rejeter')">✗ Rejeter</button>
                </td>
            </tr>
            <tr>
                <td><strong>Jean Dupont</strong></td>
                <td>Attestation <span style="color: var(--dxc-gray-600);">(Travail)</span></td>
                <td>${new Date().toLocaleDateString('fr-FR')}</td>
                <td><span class="status-badge status-approved">Approuvée</span></td>
                <td><button class="btn-icon" style="background: var(--dxc-accent); color: white;">Voir</button></td>
            </tr>
        `;
    }
}

async function approuver(source, id) {
    if (!confirm('Approuver cette demande ?')) return;
    
    try {
        const url = source === 'conges' 
            ? `${API_BASE_URL_CONGES}/${id}/approuver`
            : `${API_BASE_URL_ATTESTATIONS}/${id}/generer`;
        
        const res = await fetch(url, {
            method: 'PUT',
            headers: { 'Authorization': 'Bearer ' + token }
        });
        
        if (res.ok) {
            alert('Demande approuvée !');
            loadStats();
            loadDemandes();
        }
    } catch (error) {
        alert('Erreur: Impossible d\'approuver. Vérifiez que le backend est démarré.');
    }
}

async function rejeter(source, id) {
    const motif = prompt('Motif du rejet:');
    if (!motif) return;
    
    try {
        const url = `${API_BASE_URL_CONGES}/${id}/rejeter`;
        const res = await fetch(url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
            body: JSON.stringify({ motif })
        });
        
        if (res.ok) {
            alert('Demande rejetée');
            loadStats();
            loadDemandes();
        }
    } catch (error) {
        alert('Erreur: Impossible de rejeter. Vérifiez que le backend est démarré.');
    }
}

function logout() {
    localStorage.clear();
    sessionStorage.clear();
    window.location.href = '../html/login.html';
}

loadStats();
loadDemandes();
