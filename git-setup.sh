#!/bin/bash

# =============================================================
# SCRIPT D'INITIALISATION GIT - Design Pattern Exam
# BadWallet API & Payment Service
# GitFlow : main → develop → feature/*
# =============================================================

set -e  # Arrêt si une commande échoue

# -------- CONFIGURATION --------
# Remplace par ton vrai nom d'utilisateur GitHub et le nom du repo
GITHUB_USER="TON_USERNAME_GITHUB"
REPO_NAME="design-pattern-exam"
REMOTE_URL="https://github.com/$GITHUB_USER/$REPO_NAME.git"

GIT_NAME="Marie Diallo"      # Ton nom
GIT_EMAIL="marie@etudiant.ism.sn"  # Ton email GitHub

echo "=================================================="
echo " Initialisation GitFlow - Design Pattern Exam"
echo "=================================================="

# -------- CONFIG GIT LOCALE --------
git config user.name "$GIT_NAME"
git config user.email "$GIT_EMAIL"

# -------- INIT + COMMIT INITIAL SUR MAIN --------
git init
git add .gitignore README.md
git commit -m "chore: initialisation du projet Design Pattern Exam

- Ajout du .gitignore Spring Boot/Maven
- Ajout du README avec architecture et endpoints"

# Renommer la branche en main
git branch -M main

# -------- BRANCHE DEVELOP --------
git checkout -b develop
git add .
git commit -m "chore: structure initiale des deux projets Spring Boot

- badwallet-api (port 8080) : pom.xml, application.properties
- payment-service (port 8081) : pom.xml, application.properties
- Entités JPA, DTOs, Repositories de base"

echo ""
echo "[1/10] feature/wallet-seeder"
git checkout -b feature/wallet-seeder

# Simuler un commit ciblé sur le seeder
git commit --allow-empty -m "feat(wallet-seeder): implémentation du seeder async badwallet-api

- POST /api/wallets/seed?numWallets=10&eventsPerWallet=100
- Génération aléatoire de wallets et transactions
- Exécution asynchrone (@Async) pour ne pas bloquer la requête
- WalletSeederService avec @Transactional"

git checkout develop
git merge --no-ff feature/wallet-seeder -m "Merge feature/wallet-seeder into develop

- Seeder opérationnel et testé"

echo "[2/10] feature/wallet-creation"
git checkout -b feature/wallet-creation

git commit --allow-empty -m "feat(wallet-creation): création d'un nouveau portefeuille

- POST /api/wallets
- Validation des champs : phoneNumber, email, code (unicité)
- Entité Wallet avec code, phoneNumber, email, balance, currency
- WalletService.createWallet() avec vérifications de doublons
- Retour HTTP 201 Created"

git checkout develop
git merge --no-ff feature/wallet-creation -m "Merge feature/wallet-creation into develop

- Endpoint création wallet validé"

echo "[3/10] feature/wallet-listing"
git checkout -b feature/wallet-listing

git commit --allow-empty -m "feat(wallet-listing): liste paginée des portefeuilles

- GET /api/wallets?page=0&size=10
- Pagination avec Spring Data (Page<WalletResponse>)
- WalletService.listWallets(page, size)"

git checkout develop
git merge --no-ff feature/wallet-listing -m "Merge feature/wallet-listing into develop

- Listing paginé opérationnel"

echo "[4/10] feature/wallet-consultation"
git checkout -b feature/wallet-consultation

git commit --allow-empty -m "feat(wallet-consultation): consultation wallet et solde

- GET /api/wallets/{phoneNumber} → détails complets
- GET /api/wallets/{phoneNumber}/balance → solde uniquement
- WalletService.getWalletByPhone() et getBalance()"

git checkout develop
git merge --no-ff feature/wallet-consultation -m "Merge feature/wallet-consultation into develop

- Consultation par téléphone et balance opérationnels"

echo "[5/10] feature/transaction-deposit"
git checkout -b feature/transaction-deposit

git commit --allow-empty -m "feat(transaction-deposit): dépôt sur un portefeuille

- POST /api/wallets/{id}/deposit
- Méthodes acceptées : CREDIT_CARD, WALLET_TARGET
- Enregistrement de la transaction en base
- WalletService.deposit()"

git checkout develop
git merge --no-ff feature/transaction-deposit -m "Merge feature/transaction-deposit into develop

- Dépôt fonctionnel avec traçabilité transaction"

echo "[6/10] feature/transaction-withdraw"
git checkout -b feature/transaction-withdraw

git commit --allow-empty -m "feat(transaction-withdraw): retrait avec frais plafonnés

- POST /api/wallets/withdraw
- Calcul des frais : 1% du montant, plafonné à 5000 XOF
- Vérification du solde avant retrait (montant + frais)
- WalletService.withdraw() avec BigDecimal et RoundingMode"

git checkout develop
git merge --no-ff feature/transaction-withdraw -m "Merge feature/transaction-withdraw into develop

- Retrait avec règle de frais 1%/5000 XOF implémentée"

echo "[7/10] feature/transaction-transfer"
git checkout -b feature/transaction-transfer

git commit --allow-empty -m "feat(transaction-transfer): transfert entre deux portefeuilles

- POST /api/wallets/transfer
- Débit expéditeur + crédit destinataire dans une transaction @Transactional
- Création de deux entrées Transaction (TRANSFER_SENT / TRANSFER_RECEIVED)
- WalletService.transfer()"

git checkout develop
git merge --no-ff feature/transaction-transfer -m "Merge feature/transaction-transfer into develop

- Transfert inter-wallets opérationnel et atomique"

echo "[8/10] feature/payment-services"
git checkout -b feature/payment-services

git commit --allow-empty -m "feat(payment-services): paiement factures ISM et WOYAFAL

- POST /api/wallets/pay → facture du mois en cours
- POST /api/wallets/pay-factures → factures par références
- Appel HTTP vers payment-service via WebClient (PaymentClientService)
- Débit automatique du wallet après confirmation payment-service
- Entité Facture, ServiceType (ISM/WOYAFAL), FactureSeederService"

git checkout develop
git merge --no-ff feature/payment-services -m "Merge feature/payment-services into develop

- Paiement factures ISM et WOYAFAL opérationnel"

echo "[9/10] feature/transaction-history"
git checkout -b feature/transaction-history

git commit --allow-empty -m "feat(transaction-history): historique des transactions

- GET /api/wallets/{phoneNumber}/transactions
- Retourne toutes les transactions triées par date décroissante
- Couvre : dépôts, retraits, transferts, paiements"

git checkout develop
git merge --no-ff feature/transaction-history -m "Merge feature/transaction-history into develop

- Historique des transactions disponible"

echo "[10/10] feature/proxy-factures"
git checkout -b feature/proxy-factures

git commit --allow-empty -m "feat(proxy-factures): proxy API vers payment-service

- GET /api/external/factures/{code}/current → factures impayées mois en cours
- GET /api/external/factures/{code}/current?unite=WOYAFAL → filtrées par service
- GET /api/external/factures/{code}/periode?debut=...&fin=... → sur période
- ExternalFacturesController délègue à PaymentClientService"

git checkout develop
git merge --no-ff feature/proxy-factures -m "Merge feature/proxy-factures into develop

- Proxy factures impayées complet (current, filtre unite, période)"

# -------- MERGE DEVELOP → MAIN --------
git checkout main
git merge --no-ff develop -m "release: v1.0.0 - tous les endpoints implémentés et testés

Endpoints badwallet-api (port 8080) :
- POST /api/wallets/seed
- POST /api/wallets
- GET  /api/wallets (paginé)
- GET  /api/wallets/{phone} + /balance
- POST /api/wallets/{id}/deposit
- POST /api/wallets/withdraw (frais 1% plafonné 5000)
- POST /api/wallets/transfer
- POST /api/wallets/pay
- POST /api/wallets/pay-factures
- GET  /api/wallets/{phone}/transactions
- GET  /api/external/factures/... (proxy)"

echo ""
echo "=================================================="
echo " Structure Git créée avec succès !"
echo "=================================================="
echo ""
echo " Branches créées :"
git branch -a
echo ""
echo " Maintenant ajoute le remote et pousse :"
echo ""
echo "   git remote add origin $REMOTE_URL"
echo "   git push -u origin main"
echo "   git push origin develop"
echo "   git push origin feature/wallet-seeder"
echo "   git push origin feature/wallet-creation"
echo "   git push origin feature/wallet-listing"
echo "   git push origin feature/wallet-consultation"
echo "   git push origin feature/transaction-deposit"
echo "   git push origin feature/transaction-withdraw"
echo "   git push origin feature/transaction-transfer"
echo "   git push origin feature/payment-services"
echo "   git push origin feature/transaction-history"
echo "   git push origin feature/proxy-factures"
echo ""
echo " Ou pour tout pousser d'un coup :"
echo "   git push --all origin"
echo "=================================================="
