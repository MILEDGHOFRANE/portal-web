const API_CONGES = 'http://localhost:8085/api/conges';
const API_ATTESTATIONS = 'http://localhost:8085/api/attestations';
const token = localStorage.getItem('authToken');
const user = JSON.parse(localStorage.getItem('user') || '{}');

if (!token) {
    window.location.href = '../html/login.html';
}

if (user && user.firstName) {
    document.getElementById('userName').textContent = user.firstName;
    document.getElementById('welcomeMessage').textContent = 'Bienvenue, ' + user.firstName + '!';
}

async function loadStats() {
    try {
        const headers = { 'Authorization': 'Bearer ' + token };
        
        const [congesRes, attestationsRes, soldeRes] = await Promise.all([
            fetch(API_CONGES + '/mes-demandes', { headers }).catch(() => ({ ok: false })),
            fetch(API_ATTESTATIONS + '/mes-demandes', { headers }).catch(() => ({ ok: false })),
            fetch(API_CONGES + '/solde', { headers }).catch(() => ({ ok: false }))
        ]);
        
        if (congesRes.ok) {
            const data = await congesRes.json();
            if (data.success && data.conges) {
                document.getElementById('totalConges').textContent = data.conges.length;
            }
        }
        
        if (attestationsRes.ok) {
            const data = await attestationsRes.json();
            if (data.success && data.attestations) {
                document.getElementById('totalAttestations').textContent = data.attestations.length;
            }
        }
        
        if (soldeRes.ok) {
            const data = await soldeRes.json();
            if (data.success && data.solde) {
                document.getElementById('soldeConges').textContent = data.solde.restants;
            }
        }
    } catch (error) {
        console.log('Mode démo');
    }
}

function logout() {
    localStorage.clear();
    sessionStorage.clear();
    window.location.href = '../html/login.html';
}

loadStats();