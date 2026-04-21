# SLA Microservice

## Description
Microservice de gestion des demandes SLA (Service Level Agreement) pour le portail RH DXC.

## Port
- **Port:** 8086

## Endpoints principaux
- `GET /api/employees` — Liste tous les employés
- `GET /api/employees/{id}` — Détails d'un employé
- `GET /api/requests/my-requests/{id}` — Demandes d'un employé
- `GET /api/requests/manager/pending` — Demandes en attente (Manager)
- `GET /api/requests/hr/pending` — Demandes en attente (RH)
- `POST /api/requests/submit` — Soumettre une demande
- `POST /api/requests/{id}/approve` — Approuver une demande
- `POST /api/requests/{id}/reject` — Refuser une demande

## Technologies
- Java 21
- Spring Boot
- MySQL
- Docker

## Lancer le service
```bash
mvn clean package
java -jar target/*.jar
```