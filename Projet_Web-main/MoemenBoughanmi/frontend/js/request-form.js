// ============================================
// REQUEST FORM - JAVASCRIPT
// ============================================

const API_BASE_URL = 'http://localhost:8081/api';
let selectedServiceType = null;
let uploadedFiles = [];

// Check Authentication
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

// Service Type Definitions
const SERVICE_FORMS = {
    attestation_travail: {
        title: 'Attestation de travail',
        fields: [
            { name: 'motif', label: 'Motif de la demande', type: 'select', required: true, 
              options: ['Banque', 'Ambassade', 'Location', 'Autre'] },
            { name: 'langue', label: 'Langue', type: 'select', required: true,
              options: ['Français', 'Anglais', 'Arabe'] },
            { name: 'nombre_copies', label: 'Nombre de copies', type: 'number', required: true, min: 1, max: 5 },
            { name: 'commentaire', label: 'Commentaire', type: 'textarea', required: false }
        ]
    },
    conge: {
        title: 'Demande de congés',
        fields: [
            { name: 'type_conge', label: 'Type de congé', type: 'select', required: true,
              options: ['Congé annuel', 'Congé maladie', 'Congé exceptionnel', 'RTT'] },
            { name: 'date_debut', label: 'Date de début', type: 'date', required: true },
            { name: 'date_fin', label: 'Date de fin', type: 'date', required: true },
            { name: 'nombre_jours', label: 'Nombre de jours', type: 'number', required: true, min: 1 },
            { name: 'remplacement', label: 'Personne de remplacement', type: 'text', required: false },
            { name: 'motif', label: 'Motif', type: 'textarea', required: true }
        ]
    },
    fiche_paie: {
        title: 'Demande de fiche de paie',
        fields: [
            { name: 'mois', label: 'Mois', type: 'select', required: true,
              options: ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'] },
            { name: 'annee', label: 'Année', type: 'number', required: true, min: 2020, max: 2026 },
            { name: 'format', label: 'Format souhaité', type: 'select', required: true,
              options: ['PDF', 'Papier'] },
            { name: 'motif', label: 'Motif de la demande', type: 'textarea', required: false }
        ]
    },
    remboursement_soins: {
        title: 'Bulletin de soins (Remboursement)',
        fields: [
            { name: 'matricule', label: 'Matricule', type: 'text', required: true },
            { name: 'ref_bulletin', label: 'Référence Bulletin', type: 'text', required: true },
            { name: 'date_soins', label: 'Date des soins', type: 'date', required: true },
            { name: 'nom_adherent', label: 'Nom/Prénom de l\'adhérent', type: 'text', required: true },
            { name: 'beneficiaire', label: 'Personne du malade', type: 'select', required: true,
              options: ['Adhérent', 'Conjoint', 'Enfant'] },
            { name: 'patient_name', label: 'Nom du patient', type: 'text', required: true },
            
            // Section Détails des frais
            { name: 'honoraires', label: 'Honoraires', type: 'number', required: false, step: '0.001' },
            { name: 'pharmacie', label: 'Pharmacie', type: 'number', required: false, step: '0.001' },
            { name: 'analyse', label: 'Analyse', type: 'number', required: false, step: '0.001' },
            { name: 'radio_echo', label: 'Radio/Echo/Scanner', type: 'number', required: false, step: '0.001' },
            { name: 'materiel', label: 'Matériel/Frais divers', type: 'number', required: false, step: '0.001' },
            { name: 'optique', label: 'Optique', type: 'number', required: false, step: '0.001' },
            { name: 'dentaire', label: 'Dentaire', type: 'number', required: false, step: '0.001' },
            { name: 'chirurgie', label: 'Chirurgie', type: 'number', required: false, step: '0.001' },
            { name: 'verres', label: 'Verres', type: 'number', required: false, step: '0.001' },
            { name: 'monture', label: 'Monture', type: 'number', required: false, step: '0.001' },
            
            { name: 'total_soins', label: 'Total des Soins (TND)', type: 'number', required: true, readonly: true },
            { name: 'commentaire', label: 'Commentaire', type: 'textarea', required: false }
        ]
    },
    transport: {
        title: 'Demande de prise en charge transport',
        fields: [
            { name: 'type_transport', label: 'Type de transport', type: 'select', required: true,
              options: ['Trajet domicile-travail', 'Déplacement professionnel', 'Taxi/VTC', 'Train', 'Avion'] },
            { name: 'date_trajet', label: 'Date du trajet', type: 'date', required: true },
            { name: 'depart', label: 'Lieu de départ', type: 'text', required: true },
            { name: 'arrivee', label: 'Lieu d\'arrivée', type: 'text', required: true },
            { name: 'montant', label: 'Montant (TND)', type: 'number', required: true, step: '0.01' },
            { name: 'motif', label: 'Motif du déplacement', type: 'textarea', required: true }
        ]
    },
    autre: {
        title: 'Autre demande',
        fields: [
            { name: 'sujet', label: 'Sujet de la demande', type: 'text', required: true },
            { name: 'description', label: 'Description détaillée', type: 'textarea', required: true },
            { name: 'urgence', label: 'Niveau d\'urgence', type: 'select', required: true,
              options: ['Normal', 'Urgent', 'Très urgent'] }
        ]
    }
};

// Handle Service Card Click
document.querySelectorAll('.service-card').forEach(card => {
    card.addEventListener('click', function() {
        // Remove active class from all cards
        document.querySelectorAll('.service-card').forEach(c => c.classList.remove('active'));
        
        // Add active class to clicked card
        this.classList.add('active');
        
        // Get service type
        selectedServiceType = this.dataset.service;
        document.getElementById('selectedService').value = selectedServiceType;
        document.getElementById('serviceError').style.display = 'none';
        
        // Generate form
        generateDynamicForm(selectedServiceType);
        
        // Show attachments section
        document.getElementById('attachmentsCard').style.display = 'block';
        
        // Enable submit button
        document.getElementById('submitBtn').disabled = false;
    });
});

// Generate Dynamic Form
function generateDynamicForm(serviceType) {
    const formConfig = SERVICE_FORMS[serviceType];
    const dynamicForm = document.getElementById('dynamicForm');
    
    let html = `
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">${formConfig.title}</h3>
            </div>
            <div class="card-body">
    `;
    
    // Group fields by section (remboursement_soins has sections)
    if (serviceType === 'remboursement_soins') {
        // Basic info
        html += '<h4 class="section-title">Informations de base</h4><div class="form-row">';
        formConfig.fields.slice(0, 6).forEach(field => {
            html += generateFieldHTML(field);
        });
        html += '</div>';
        
        // Détails des frais
        html += '<h4 class="section-title">Détails des frais</h4><div class="form-row">';
        formConfig.fields.slice(6, -2).forEach(field => {
            html += generateFieldHTML(field);
        });
        html += '</div>';
        
        // Total and comments
        html += '<div class="form-row">';
        formConfig.fields.slice(-2).forEach(field => {
            html += generateFieldHTML(field);
        });
        html += '</div>';
    } else {
        html += '<div class="form-row">';
        formConfig.fields.forEach(field => {
            html += generateFieldHTML(field);
        });
        html += '</div>';
    }
    
    html += `
            </div>
        </div>
    `;
    
    dynamicForm.innerHTML = html;
    dynamicForm.style.display = 'block';
    
    // Add event listeners for medical costs calculation
    if (serviceType === 'remboursement_soins') {
        setupMedicalCostsCalculation();
    }
    
    // Add date validation for conge
    if (serviceType === 'conge') {
        setupLeaveValidation();
    }
}

// Generate Field HTML
function generateFieldHTML(field) {
    let html = '<div class="form-group">';
    html += `<label for="${field.name}" class="form-label ${field.required ? 'required' : ''}">${field.label}</label>`;
    
    if (field.type === 'select') {
        html += `<select id="${field.name}" name="${field.name}" class="form-control" ${field.required ? 'required' : ''}>`;
        html += '<option value="">Choisir une option...</option>';
        field.options.forEach(opt => {
            html += `<option value="${opt}">${opt}</option>`;
        });
        html += '</select>';
    } else if (field.type === 'textarea') {
        html += `<textarea id="${field.name}" name="${field.name}" class="form-control" rows="4" ${field.required ? 'required' : ''}></textarea>`;
    } else {
        html += `<input type="${field.type}" id="${field.name}" name="${field.name}" class="form-control" 
                 ${field.required ? 'required' : ''}
                 ${field.min ? `min="${field.min}"` : ''}
                 ${field.max ? `max="${field.max}"` : ''}
                 ${field.step ? `step="${field.step}"` : ''}
                 ${field.readonly ? 'readonly' : ''}
                 ${field.type === 'number' ? 'value="0"' : ''}>`;
    }
    
    html += '</div>';
    return html;
}

// Setup Medical Costs Calculation
function setupMedicalCostsCalculation() {
    const costFields = ['honoraires', 'pharmacie', 'analyse', 'radio_echo', 'materiel', 'optique', 'dentaire', 'chirurgie', 'verres', 'monture'];
    
    costFields.forEach(fieldName => {
        const input = document.getElementById(fieldName);
        if (input) {
            input.addEventListener('input', calculateTotalCosts);
        }
    });
}

// Calculate Total Costs
function calculateTotalCosts() {
    const costFields = ['honoraires', 'pharmacie', 'analyse', 'radio_echo', 'materiel', 'optique', 'dentaire', 'chirurgie', 'verres', 'monture'];
    let total = 0;
    
    costFields.forEach(fieldName => {
        const input = document.getElementById(fieldName);
        if (input) {
            total += parseFloat(input.value) || 0;
        }
    });
    
    const totalInput = document.getElementById('total_soins');
    if (totalInput) {
        totalInput.value = total.toFixed(3);
    }
}

// Setup Leave Validation
function setupLeaveValidation() {
    const dateDebut = document.getElementById('date_debut');
    const dateFin = document.getElementById('date_fin');
    const nombreJours = document.getElementById('nombre_jours');
    
    function calculateDays() {
        if (dateDebut.value && dateFin.value) {
            const start = new Date(dateDebut.value);
            const end = new Date(dateFin.value);
            const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
            nombreJours.value = days > 0 ? days : 0;
        }
    }
    
    dateDebut.addEventListener('change', calculateDays);
    dateFin.addEventListener('change', calculateDays);
}

// File Upload Handling
const dropZone = document.getElementById('dropZone');
const fileInput = document.getElementById('fileInput');
const fileList = document.getElementById('fileList');

// Click to upload
dropZone.addEventListener('click', () => fileInput.click());

// Drag and drop
dropZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropZone.classList.add('drag-over');
});

dropZone.addEventListener('dragleave', () => {
    dropZone.classList.remove('drag-over');
});

dropZone.addEventListener('drop', (e) => {
    e.preventDefault();
    dropZone.classList.remove('drag-over');
    handleFiles(e.dataTransfer.files);
});

// File input change
fileInput.addEventListener('change', (e) => {
    handleFiles(e.target.files);
});

// Handle Files
function handleFiles(files) {
    Array.from(files).forEach(file => {
        // Validate file size (10 MB max)
        if (file.size > 10 * 1024 * 1024) {
            alert(`Le fichier ${file.name} dépasse 10 MB`);
            return;
        }
        
        uploadedFiles.push(file);
        displayFile(file);
    });
}

// Display File
function displayFile(file) {
    const fileItem = document.createElement('div');
    fileItem.className = 'file-item';
    fileItem.innerHTML = `
        <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z"/>
        </svg>
        <div>
            <strong>${file.name}</strong>
            <small>${(file.size / 1024).toFixed(2)} KB</small>
        </div>
        <button type="button" class="btn-remove" onclick="removeFile('${file.name}')">
            <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"/>
            </svg>
        </button>
    `;
    fileList.appendChild(fileItem);
}

// Remove File
function removeFile(fileName) {
    uploadedFiles = uploadedFiles.filter(f => f.name !== fileName);
    // Re-render file list
    fileList.innerHTML = '';
    uploadedFiles.forEach(displayFile);
}

// Cancel Request
function cancelRequest() {
    if (confirm('Êtes-vous sûr de vouloir annuler ? Toutes les données seront perdues.')) {
        window.location.href = '../html/employee-dashboard.html';
    }
}

// Handle Form Submit
document.getElementById('requestForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    if (!selectedServiceType) {
        document.getElementById('serviceError').style.display = 'block';
        return;
    }
    
    const user = checkAuth();
    const formConfig = SERVICE_FORMS[selectedServiceType];
    
    // Collect form data
    const formData = {
        userId: user.id,
        serviceType: selectedServiceType,
        formData: {}
    };
    
    formConfig.fields.forEach(field => {
        const input = document.getElementById(field.name);
        if (input) {
            formData.formData[field.name] = input.value;
        }
    });
    
    console.log('📋 Demande à soumettre:', formData);
    console.log('📎 Fichiers joints:', uploadedFiles.length);
    
    // Show success message
    alert('✅ Demande soumise avec succès !\nVotre demande sera traitée dans les plus brefs délais.');
    
    // Redirect to requests list
    window.location.href = '../html/requests-list.html';
});

// Initialize
checkAuth();
console.log('📝 Formulaire de demande chargé');