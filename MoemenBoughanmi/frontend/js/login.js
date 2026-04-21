const API_BASE_URL = 'http://localhost:8083/api';
const STANDALONE_MODE = false;

document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    const errorAlert = document.getElementById('errorAlert');
    const errorMessage = document.getElementById('errorMessage');
    errorAlert.classList.add('dxc-hidden');
    
    const loginBtn = document.getElementById('loginBtn');
    loginBtn.disabled = true;
    loginBtn.textContent = 'Connexion...';
    
    try {
        const response = await fetch(API_BASE_URL + '/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        
        const data = await response.json();
        
        if (data.success) {
            if (data.requiresOtp) {
                sessionStorage.setItem('otpEmail', email);
                window.location.href = '../html/otp-verification.html';
            } else {
                localStorage.setItem('authToken', data.token);
                localStorage.setItem('user', JSON.stringify(data.user));
                localStorage.setItem('isLoggedIn', 'true');
                
                window.location.href = '../html/dashboard.html';
            }
        } else {
            errorMessage.textContent = data.message || 'Erreur de connexion';
            errorAlert.classList.remove('dxc-hidden');
        }
    } catch (error) {
        console.error('Erreur:', error);
        errorMessage.textContent = 'Erreur de connexion au serveur';
        errorAlert.classList.remove('dxc-hidden');
    } finally {
        loginBtn.disabled = false;
        loginBtn.textContent = 'Se connecter';
    }
});

document.getElementById('togglePassword').addEventListener('click', function() {
    const passwordInput = document.getElementById('password');
    passwordInput.type = passwordInput.type === 'password' ? 'text' : 'password';
});