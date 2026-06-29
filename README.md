# Design Pattern Exam - BadWallet API & Payment Service

## Architecture

Deux microservices Spring Boot Maven :

| Service | Port | Description |
|---------|------|-------------|
| `badwallet-api` | 8080 | Gestion des portefeuilles électroniques |
| `payment-service` | 8081 | Gestion des factures ISM et WOYAFAL |

## Prérequis

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## Configuration

### Créer les bases de données

```sql
CREATE DATABASE badwallet_db;
CREATE DATABASE payment_db;
```

### Variables à adapter dans `application.properties`

```properties
spring.datasource.username=postgres
spring.datasource.password=votre_mot_de_passe
```

## Lancer les projets

```bash
# Terminal 1 - badwallet-api
cd badwallet-api
mvn spring-boot:run

# Terminal 2 - payment-service
cd payment-service
mvn spring-boot:run
```

## Endpoints badwallet-api (port 8080)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/wallets/seed` | Seeder la BDD |
| POST | `/api/wallets` | Créer un portefeuille |
| GET | `/api/wallets?page=0&size=10` | Lister (paginé) |
| GET | `/api/wallets/{phone}` | Consulter par téléphone |
| GET | `/api/wallets/{phone}/balance` | Solde uniquement |
| POST | `/api/wallets/{id}/deposit` | Dépôt |
| POST | `/api/wallets/withdraw` | Retrait (frais 1% plafonné 5000) |
| POST | `/api/wallets/transfer` | Transfert entre wallets |
| POST | `/api/wallets/pay` | Payer facture du mois en cours |
| POST | `/api/wallets/pay-factures` | Payer factures spécifiques |
| GET | `/api/wallets/{phone}/transactions` | Historique |
| GET | `/api/external/factures/{code}/current` | Factures impayées du mois |
| GET | `/api/external/factures/{code}/periode` | Factures sur période |

## Stratégie Git (GitFlow)

```
main
└── develop
    ├── feature/wallet-seeder
    ├── feature/wallet-creation
    ├── feature/wallet-listing
    ├── feature/wallet-consultation
    ├── feature/transaction-deposit
    ├── feature/transaction-withdraw
    ├── feature/transaction-transfer
    ├── feature/payment-services
    ├── feature/transaction-history
    └── feature/proxy-factures
```
