# 👤 DXC Profiles Microservice

Microservice de gestion des profils utilisateurs.

---

## 🚀 DÉMARRAGE

### Prérequis
- Java 21
- Maven 3.8+

### Lancer le microservice
```bash
cd profiles-microservice
mvn clean install
mvn spring-boot:run
```

Le service démarre sur **http://localhost:8084**

---

## 📋 ENDPOINTS

### GET /api/profiles/me
Récupère le profil de l'utilisateur connecté

**Headers :**
```
Authorization: Bearer <JWT_TOKEN>
```

**Réponse :**
```json
{
  "id": 1,
  "userId": 1,
  "email": "user@dxc.com",
  "firstName": "John",
  "lastName": "Doe",
  "nationalId": "07039118",
  "residentialAddress": "05, Avenue De Paris",
  "contactPhone": "98603014",
  "familyStatus": "Married (1 Kids)",
  "professionalTitle": "CTO",
  "corporateEmail": "john.doe@dxc.com",
  "role": "Admin"
}
```

---

### POST /api/profiles/me
Créer son profil

**Headers :**
```
Authorization: Bearer <JWT_TOKEN>
```

**Body :**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "nationalId": "07039118",
  "contactPhone": "98603014",
  "residentialAddress": "05, Avenue De Paris Boumhel",
  "familyStatus": "Married (1 Kids)",
  "professionalTitle": "CTO",
  "department": "IT",
  "technicalSkills": "[\"Java\", \"Spring Boot\", \"React\"]"
}
```

---

### PUT /api/profiles/me
Modifier son profil

**Headers :**
```
Authorization: Bearer <JWT_TOKEN>
```

**Body :**
```json
{
  "contactPhone": "99999999",
  "residentialAddress": "Nouvelle adresse"
}
```

---

### GET /api/profiles/all
Liste tous les profils (admin)

---

### GET /api/profiles/{userId}
Profil d'un utilisateur spécifique

---

### GET /api/profiles/health
Health check

**Réponse :**
```json
{
  "status": "UP",
  "service": "profiles-microservice"
}
```

---

## 💾 BASE DE DONNÉES

**Type :** H2 (fichier persistant)  
**Location :** `./data/profiles_db`  
**Console :** http://localhost:8084/h2-console

**JDBC URL :** `jdbc:h2:file:./data/profiles_db`  
**Username :** `sa`  
**Password :** (vide)

---

## 🧪 TESTS AVEC POSTMAN

### 1. Obtenir un JWT token
D'abord, connecte-toi via le microservice Auth (8083) pour obtenir un token

### 2. Créer ton profil
```
POST http://localhost:8084/api/profiles/me
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "firstName": "Moemen",
  "lastName": "Boughanmi",
  "nationalId": "12345678",
  "contactPhone": "98603014"
}
```

### 3. Récupérer ton profil
```
GET http://localhost:8084/api/profiles/me
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### 4. Modifier ton profil
```
PUT http://localhost:8084/api/profiles/me
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "contactPhone": "11111111"
}
```

---

## 🔐 SÉCURITÉ

- Utilise JWT tokens du microservice Auth
- Chaque utilisateur ne peut voir/modifier que SON profil
- L'endpoint `/all` est accessible à tous (à sécuriser selon besoins)

---

## 📊 ARCHITECTURE

```
┌─────────────────────────────────┐
│  Frontend (Port 5500)          │
└────────────┬────────────────────┘
             │
      ┌──────┼──────┐
      │             │
      ▼             ▼
┌──────────┐  ┌────────────┐
│  Auth    │  │  Profiles  │
│  8083    │  │   8084     │
└──────────┘  └────────────┘
     │              │
     ▼              ▼
 auth_db      profiles_db
  (H2)           (H2)
```

---

## ✅ CHECKLIST

- [ ] Microservice lancé (port 8084)
- [ ] JWT token obtenu depuis Auth (8083)
- [ ] Profil créé avec POST /me
- [ ] Profil récupéré avec GET /me
- [ ] Profil modifié avec PUT /me
- [ ] Health check OK

---

**🚀 Le microservice Profiles est prêt ! 💪**
