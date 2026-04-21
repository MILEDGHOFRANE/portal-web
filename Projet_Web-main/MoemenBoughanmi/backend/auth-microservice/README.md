#  DXC Authentication Microservice

Microservice d'authentification avec vérification email et OTP (One-Time Password).

##  Fonctionnalités

-  **Inscription** avec vérification email
-  **Vérification de compte** par lien email
-  **Authentification 2FA** avec code OTP par email
-  **JWT tokens** pour sessions sécurisées
-  **Protection contre force brute** (verrouillage après 5 tentatives)
-  **Emails HTML** professionnels

---

##  Démarrage Rapide

### Prérequis
- Java 21
- Maven 3.8+
- Compte Gmail (pour l'envoi d'emails)

### Configuration Email (IMPORTANT!)

1. **Créer un mot de passe d'application Gmail :**
   - Va sur : https://myaccount.google.com/apppasswords
   - Crée un nouveau mot de passe d'application
   - Copie le mot de passe (16 caractères)

2. **Modifier `application.properties` :**
   ```properties
   spring.mail.username=TON_EMAIL@gmail.com
   spring.mail.password=TON_MOT_DE_PASSE_APPLICATION
   ```

### Lancement
```bash
cd auth-microservice
mvn clean install
mvn spring-boot:run
```

Le service démarre sur **http://localhost:8083**

---

##  API Endpoints

### 1. Inscription
```bash
POST http://localhost:8083/api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Réponse :**
```json
{
  "success": true,
  "message": "Inscription réussie! Vérifiez votre email.",
  "email": "user@example.com"
}
```

** Email envoyé** avec lien de vérification.

---

### 2. Vérification Email
```bash
GET http://localhost:8083/api/auth/verify?token=abc-123-xyz
```

**Réponse :**
```json
{
  "success": true,
  "message": "Compte vérifié! Vous pouvez vous connecter."
}
```

** Email de bienvenue envoyé.**

---

### 3. Connexion - Étape 1
```bash
POST http://localhost:8083/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Réponse :**
```json
{
  "success": true,
  "message": "Code envoyé par email.",
  "email": "user@example.com",
  "requiresOtp": true
}
```

** Email OTP envoyé** (code 6 chiffres, expire en 5 min).

---

### 4. Connexion - Étape 2 (Vérification OTP)
```bash
POST http://localhost:8083/api/auth/verify-otp
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Réponse :**
```json
{
  "success": true,
  "message": "Connexion réussie!",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER"
  }
}
```

---

### 5. Health Check
```bash
GET http://localhost:8083/api/auth/health
```

**Réponse :**
```json
{
  "status": "UP",
  "service": "auth-microservice",
  "version": "1.0.0"
}
```

---

##  Base de Données H2

**Console H2 :** http://localhost:8083/h2-console

**Login :**
- JDBC URL : `jdbc:h2:mem:auth_db`
- Username : `sa`
- Password : (vide)

**Tables créées :**
- `auth_users` - Utilisateurs
- `verification_tokens` - Tokens de vérification email
- `otp_codes` - Codes OTP temporaires

---

##  Sécurité

### Mot de passe
- Cryptage **BCrypt**
- Minimum 8 caractères requis
- Stockage sécurisé (jamais en clair)

### Protection anti-brute force
- Verrouillage automatique après 5 tentatives échouées
- Compteur de tentatives par utilisateur

### JWT Tokens
- Expiration : 24 heures
- Signature sécurisée
- Claims: userId, email, role

### OTP
- Expire après 5 minutes
- Usage unique
- 6 chiffres aléatoires

---

##  Templates Email

### Email de vérification
- Design professionnel HTML
- Lien d'activation valable 24h
- Branding DXC

### Email OTP
- Code 6 chiffres en grand
- Expiration claire (5 min)
- Avertissement sécurité

### Email de bienvenue
- Envoyé après vérification
- Lien vers le portail

---

##  Tests avec Postman

1. **Register** → Vérifier email → Copier token
2. **Verify** avec token → Compte activé
3. **Login** → Vérifier email OTP
4. **Verify-OTP** → Recevoir JWT token
5. Utiliser JWT pour appels authentifiés

---

##  Configuration

### Variables importantes (`application.properties`)
```properties
# Port
server.port=8083

# Email SMTP
spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_APP_PASSWORD

# JWT Secret (change en production!)
app.jwt.secret=CHANGE-THIS-SECRET-KEY

# OTP
app.otp.length=6
app.otp.expiration-minutes=5

# Verification
app.verification.expiration-hours=24

# Frontend URL
app.frontend.url=http://localhost:5500
```

---

##  Troubleshooting

### Email ne s'envoie pas
- Vérifier `spring.mail.username` et `spring.mail.password`
- Vérifier que le mot de passe est un "App Password" Gmail
- Vérifier les logs : `logging.level.org.springframework.mail=DEBUG`

### Erreur "Account locked"
- Compte verrouillé après 5 tentatives
- Réinitialiser : `UPDATE auth_users SET is_locked=false, failed_login_attempts=0`

### JWT invalide
- Vérifier que `app.jwt.secret` est identique partout
- Vérifier l'expiration du token

---

##  Architecture

```
┌─────────────┐
│  Frontend   │
└──────┬──────┘ │
       ▼
┌─────────────────────────────┐
│  Auth Microservice (8083)   │
│  ┌─────────────────────┐    │
│  │ AuthController      │    │
│  └─────────┬───────────┘    │
│            │                │
│  ┌─────────▼───────────┐    │
│  │ AuthService         │    │
│  │  - Register         │    │
│  │  - Login            │    │
│  │  - Verify           │    │
│  └─────────┬───────────┘    │
│            │                │
│  ┌─────────▼───────────┐    │
│  │ EmailService        │────┼──► 📧
│  │ JwtService          │    │
│  └─────────┬───────────┘    │
│            │                │
│  ┌─────────▼───────────┐    │
│  │ H2 Database         │    │
│  │  - auth_users       │    │
│  │  - otp_codes        │    │
│  │  - verification...  │    │
│  └─────────────────────┘    │
└─────────────────────────────┘
```

---


---

 Ce microservice est prêt pour la production après configuration email !
