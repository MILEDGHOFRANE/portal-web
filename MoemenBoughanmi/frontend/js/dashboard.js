// Configuration
const API_BASE_URL = 'http://localhost:8083/api';

// Vérification d'authentification
function checkAuth() {
    console.log('🏠 Vérification auth...');
    const token = localStorage.getItem('authToken');
    const isLoggedIn = localStorage.getItem('isLoggedIn');
    const userStr = localStorage.getItem('user');
    
    if (!token || !userStr || isLoggedIn !== 'true') {
        console.error('❌ Session invalide ou expirée');
        localStorage.clear();
        window.location.href = 'login.html';
        return null;
    }

    const user = JSON.parse(userStr);
    console.log('✅ Utilisateur connecté:', user.email, 'Rôle:', user.role);

    // Redirection basée sur le rôle si on est sur la mauvaise page
    // Note: dashboard.html est pour ADMIN et RH
    const currentPage = window.location.pathname.split('/').pop();
    
    if (user.role === 'EMPLOYEE' && currentPage === 'dashboard.html') {
        window.location.href = 'employee-dashboard.html';
        return null;
    }

    return user;
}

// Logout
function logout() {
    localStorage.clear();
    sessionStorage.clear();
    window.location.href = 'login.html';
}

// Format Date
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

// Get Status Badge
function getStatusBadge(status) {
    const badges = {
        'PENDING': '<span class="badge badge-pending">En attente</span>',
        'IN_REVIEW': '<span class="badge badge-in-review">En révision</span>',
        'APPROVED': '<span class="badge badge-approved">Approuvée</span>',
        'REJECTED': '<span class="badge badge-rejected">Rejetée</span>',
        'CANCELLED': '<span class="badge bg-secondary">Annulée</span>'
    };
    return badges[status] || `<span class="badge bg-info">${status}</span>`;
}

// Load Dashboard Data
async function loadDashboard() {
    const user = checkAuth();
    if (!user) return;
    
    // Set user name
    const userNameElement = document.getElementById('userName');
    if (userNameElement) {
        userNameElement.textContent = `${user.firstName} ${user.lastName}`;
    }
    
    try {
        // Load KPIs
        await loadKPIs();
        
        // Load Charts
        if (typeof Chart !== 'undefined') {
            await loadCharts();
        }
        
        // Load Recent Requests
        await loadRecentRequests();
    } catch (error) {
        console.error('Erreur chargement dashboard:', error);
    }
}

// Load KPIs
async function loadKPIs() {
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard/rh`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('authToken')}` }
        });
        
        if (!response.ok) {
            showTestData();
            return;
        }
        
        const data = await response.json();
        
        if (document.getElementById('kpiPending')) document.getElementById('kpiPending').textContent = data.pending || 0;
        if (document.getElementById('kpiReview')) document.getElementById('kpiReview').textContent = data.in_review || 0;
        if (document.getElementById('kpiApproved')) document.getElementById('kpiApproved').textContent = data.approved_today || 0;
        if (document.getElementById('kpiOverdue')) document.getElementById('kpiOverdue').textContent = data.overdue || 0;
    } catch (error) {
        console.log('API non disponible, affichage des données de test');
        showTestData();
    }
}

// Show Test Data
function showTestData() {
    if (document.getElementById('kpiPending')) document.getElementById('kpiPending').textContent = '12';
    if (document.getElementById('kpiReview')) document.getElementById('kpiReview').textContent = '5';
    if (document.getElementById('kpiApproved')) document.getElementById('kpiApproved').textContent = '8';
    if (document.getElementById('kpiOverdue')) document.getElementById('kpiOverdue').textContent = '2';
}

// Load Charts
async function loadCharts() {
    const ctx1 = document.getElementById('requestsChart');
    if (ctx1) {
        new Chart(ctx1, {
            type: 'line',
            data: {
                labels: ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'],
                datasets: [
                    {
                        label: 'Demandes reçues',
                        data: [12, 19, 15, 17, 14, 10, 8],
                        borderColor: '#FF6B35',
                        backgroundColor: 'rgba(255, 107, 53, 0.1)',
                        tension: 0.4
                    }
                ]
            },
            options: { responsive: true, maintainAspectRatio: false }
        });
    }
}

// Load Recent Requests
async function loadRecentRequests() {
    const tbody = document.getElementById('requestsTableBody');
    if (!tbody) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/requests?status=PENDING`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('authToken')}` }
        });
        
        if (!response.ok) throw new Error('API Error');
        
        const requests = await response.json();
        
        tbody.innerHTML = requests.slice(0, 10).map(req => `
            <tr>
                <td><strong>${req.requestNumber || req.id}</strong></td>
                <td>${req.firstName} ${req.lastName}</td>
                <td>${req.type}</td>
                <td>${formatDate(req.requestDate)}</td>
                <td>${getStatusBadge(req.status)}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="alert('Détails ID: ${req.id}')">Voir</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        showTestRequests(tbody);
    }
}

function showTestRequests(tbody) {
    const testData = [
        { ref: 'REQ-001', name: 'Jean Dupont', type: 'Congés', date: new Date(), status: 'PENDING' },
        { ref: 'REQ-002', name: 'Marie Leroy', type: 'Attestation', date: new Date(), status: 'IN_REVIEW' }
    ];
    tbody.innerHTML = testData.map(req => `
        <tr>
            <td><strong>${req.ref}</strong></td>
            <td>${req.name}</td>
            <td>${req.type}</td>
            <td>${formatDate(req.date)}</td>
            <td>${getStatusBadge(req.status)}</td>
            <td><button class="btn btn-sm btn-outline-primary">Voir</button></td>
        </tr>
    `).join('');
}

document.addEventListener('DOMContentLoaded', loadDashboard);
