
const API_BASE_URL = 'http://localhost:8086/api';
let currentPage = 1;
let totalPages = 1;
let callsData = [];
let volumeChart, callTypeChart;


function checkAuth() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if (!user.id) {
        window.location.href = '../html/login.html';
        return null;
    }
    document.getElementById('userName').textContent = `${user.firstName} ${user.lastName}`;
    return user;
}

function logout() {
    localStorage.clear();
    window.location.href = '../html/login.html';
}


function generateTestData() {
    const data = [];
    const now = new Date();
    
    for (let i = 0; i < 100; i++) {
        const timestamp = new Date(now.getTime() - (i * 5 * 60 * 1000)); // Every 5 minutes
        const callType = Math.random() > 0.6 ? 'Inbound' : 'Outbound';
        const duration = Math.floor(Math.random() * 300) + 10; // 10-310 seconds
        const answered = Math.random() > 0.15; // 85% answered
        const slaThreshold = 20; // 20 seconds
        const slaOk = answered && duration <= slaThreshold;
        
        data.push({
            timestamp: timestamp.toISOString(),
            call_type: callType,
            duration: duration,
            answered: answered,
            sla_ok: slaOk,
            account: 'Luxottica_Tunisia'
        });
    }
    
    return data.reverse(); 
}

// 
async function loadDashboard() {
    try {
        
        const response = await fetch(`${API_BASE_URL}/sla/dashboard`);
        if (response.ok) {
            const data = await response.json();
            callsData = data.calls || [];
        } else {
            throw new Error('API not available');
        }
    } catch (error) {
        console.log('⚠️ API non disponible, utilisation des données de test');
        callsData = generateTestData();
    }
    
    updateKPIs();
    updateCharts();
    updateTable();
    updateLastUpdate();
}


function updateKPIs() {
    const totalCalls = callsData.length;
    const successCalls = callsData.filter(c => c.sla_ok).length;
    const failedCalls = totalCalls - successCalls;
    const slaRate = totalCalls > 0 ? ((successCalls / totalCalls) * 100).toFixed(2) : 0;
    
    document.getElementById('kpiSlaGlobal').textContent = `${slaRate}%`;
    document.getElementById('kpiTotalCalls').textContent = totalCalls;
    document.getElementById('kpiSuccessCalls').textContent = successCalls;
    document.getElementById('kpiFailedCalls').textContent = failedCalls;
    
    
    const trend = document.getElementById('slaTrend');
    if (slaRate >= 80) {
        trend.innerHTML = `<span style="color: #28A745;">▲ Objectif atteint</span>`;
    } else if (slaRate >= 70) {
        trend.innerHTML = `<span style="color: #FFC107;">⚠ Proche de l'objectif</span>`;
    } else {
        trend.innerHTML = `<span style="color: #DC3545;">▼ En dessous de l'objectif</span>`;
    }
}


function updateCharts() {
    
    const hourlyData = {};
    callsData.forEach(call => {
        const hour = new Date(call.timestamp).getHours();
        hourlyData[hour] = (hourlyData[hour] || 0) + 1;
    });
    
    const hours = Object.keys(hourlyData).sort((a, b) => a - b);
    const volumes = hours.map(h => hourlyData[h]);
    
    if (volumeChart) volumeChart.destroy();
    
    const ctx1 = document.getElementById('volumeChart');
    volumeChart = new Chart(ctx1, {
        type: 'line',
        data: {
            labels: hours.map(h => `${h}h`),
            datasets: [{
                label: 'Volume d\'appels',
                data: volumes,
                borderColor: '#FF6B35',
                backgroundColor: 'rgba(255, 107, 53, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: { beginAtZero: true }
            }
        }
    });
    
    
    const inbound = callsData.filter(c => c.call_type === 'Inbound').length;
    const outbound = callsData.filter(c => c.call_type === 'Outbound').length;
    
    if (callTypeChart) callTypeChart.destroy();
    
    const ctx2 = document.getElementById('callTypeChart');
    callTypeChart = new Chart(ctx2, {
        type: 'doughnut',
        data: {
            labels: ['Inbound', 'Outbound'],
            datasets: [{
                data: [inbound, outbound],
                backgroundColor: ['#FF6B35', '#1A1F71'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}


function updateTable() {
    const tbody = document.getElementById('callsTableBody');
    const itemsPerPage = 20;
    const start = (currentPage - 1) * itemsPerPage;
    const end = start + itemsPerPage;
    const pageData = callsData.slice(start, end);
    
    if (pageData.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">Aucune donnée disponible</td></tr>';
        return;
    }
    
    tbody.innerHTML = pageData.map(call => {
        const time = new Date(call.timestamp).toLocaleTimeString('fr-FR');
        const slaStatus = call.sla_ok 
            ? '<span class="badge badge-approved">SLA OK</span>'
            : '<span class="badge badge-rejected">SLA KO</span>';
        const answeredStatus = call.answered
            ? '<span class="badge badge-approved">Oui</span>'
            : '<span class="badge badge-rejected">Non</span>';
        
        return `
            <tr>
                <td>${time}</td>
                <td><span class="badge ${call.call_type === 'Inbound' ? 'badge-info' : 'badge-normal'}">${call.call_type}</span></td>
                <td>${call.duration}s</td>
                <td>${answeredStatus}</td>
                <td>${call.duration}s / 20s</td>
                <td>${slaStatus}</td>
            </tr>
        `;
    }).join('');
    
    
    totalPages = Math.ceil(callsData.length / itemsPerPage);
    document.getElementById('pageInfo').textContent = `Page ${currentPage} / ${totalPages}`;
}


function nextPage() {
    if (currentPage < totalPages) {
        currentPage++;
        updateTable();
    }
}

function previousPage() {
    if (currentPage > 1) {
        currentPage--;
        updateTable();
    }
}


function filterByAccount() {
    const account = document.getElementById('accountFilter').value;
    
    console.log('Filtre compte:', account);
    loadDashboard();
}

function filterByPeriod() {
    const period = document.getElementById('periodFilter').value;

    console.log('Filtre période:', period);
    loadDashboard();
}

// 
function refreshData() {
    console.log('🔄 Actualisation des données...');
    loadDashboard();
}


function exportToCSV() {
    const headers = ['Horodatage', 'Type', 'Durée (s)', 'Répondu', 'SLA'];
    const rows = callsData.map(call => [
        call.timestamp,
        call.call_type,
        call.duration,
        call.answered ? 'Oui' : 'Non',
        call.sla_ok ? 'OK' : 'KO'
    ]);
    
    let csv = headers.join(',') + '\n';
    rows.forEach(row => {
        csv += row.join(',') + '\n';
    });
    
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `sla-export-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    
    console.log('📥 Export CSV généré');
}

// Update Last Update Time
function updateLastUpdate() {
    const now = new Date();
    const time = now.toLocaleTimeString('fr-FR');
    document.getElementById('lastUpdate').textContent = time;
}

// Auto-refresh every 30 seconds
setInterval(() => {
    refreshData();
}, 30000);

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadDashboard();
    console.log('📊 Dashboard SLA chargé');
});
