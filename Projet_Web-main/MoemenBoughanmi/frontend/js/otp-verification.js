const API_URL = 'http://localhost:8083/api/auth';
const email = sessionStorage.getItem('otpEmail');

if (!email) {
    window.location.href = '../html/login.html';
}

document.getElementById('emailDisplay').textContent = email;

document.getElementById('verifyForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const otp = Array.from(document.querySelectorAll('.otp-input'))
        .map(input => input.value)
        .join('');
    
    if (otp.length !== 6) {
        alert('Code incomplet');
        return;
    }
    
    try {
        const response = await fetch(API_URL + '/verify-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, otp: otp })
        });
        
        const data = await response.json();
        
        if (data.success) {
            localStorage.setItem('authToken', data.token);
            localStorage.setItem('user', JSON.stringify(data.user));
            localStorage.setItem('isLoggedIn', 'true');
            sessionStorage.removeItem('otpEmail');
            
            if (data.user.role === 'RH' || data.user.role === 'ADMIN') {
                window.location.href = '../html/hr-dashboard.html';
            } else if (data.user.role === 'MANAGER') {
                window.location.href = '../html/manager-dashboard.html';
            } else {
                window.location.href = '../html/employee-dashboard.html';
            }
        } else {
            alert(data.message || 'Code invalide');
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur de connexion');
    }
});

document.querySelectorAll('.otp-input').forEach((input, index, inputs) => {
    input.addEventListener('input', (e) => {
        if (e.target.value && index < inputs.length - 1) {
            inputs[index + 1].focus();
        }
    });
    
    input.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' && !e.target.value && index > 0) {
            inputs[index - 1].focus();
        }
    });
});