package com.badwallet.controller;

import com.badwallet.dto.*;
import com.badwallet.service.WalletService;
import com.badwallet.seeder.WalletSeederService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletSeederService walletSeederService;

    // 1.1 Seeder la base de données (async)
    @PostMapping("/seed")
    public ResponseEntity<Map<String, String>> seed(
            @RequestParam(defaultValue = "10") int numWallets,
            @RequestParam(defaultValue = "100") int eventsPerWallet) {
        walletSeederService.seedAsync(numWallets, eventsPerWallet);
        return ResponseEntity.accepted().body(Map.of(
                "status", "Seeding en cours",
                "wallets", String.valueOf(numWallets),
                "eventsPerWallet", String.valueOf(eventsPerWallet)
        ));
    }

    // 1.2 Créer un portefeuille
    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody WalletCreateRequest request) {
        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 1.3 Lister tous les portefeuilles (paginé)
    @GetMapping
    public ResponseEntity<Page<WalletResponse>> listWallets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(walletService.listWallets(page, size));
    }

    // 1.4 Consulter un portefeuille par numéro de téléphone
    @GetMapping("/{phoneNumber}")
    public ResponseEntity<WalletResponse> getWalletByPhone(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(walletService.getWalletByPhone(phoneNumber));
    }

    // 1.5 Consulter uniquement le solde
    @GetMapping("/{phoneNumber}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String phoneNumber) {
        BigDecimal balance = walletService.getBalance(phoneNumber);
        return ResponseEntity.ok(Map.of(
                "phoneNumber", phoneNumber,
                "balance", balance
        ));
    }

    // 1.6 Effectuer un dépôt
    @PostMapping("/{id}/deposit")
    public ResponseEntity<WalletResponse> deposit(
            @PathVariable Long id,
            @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(walletService.deposit(id, request));
    }

    // 1.7 Effectuer un retrait (frais 1% plafonné à 5000)
    @PostMapping("/withdraw")
    public ResponseEntity<WalletResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(walletService.withdraw(request));
    }

    // 1.8 Transfert entre deux portefeuilles
    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transfer(@Valid @RequestBody TransferRequest request) {
        String message = walletService.transfer(request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // 1.9 Payer une facture du mois en cours (ISM ou WOYAFAL)
    @PostMapping("/pay")
    public ResponseEntity<Map<String, String>> pay(@Valid @RequestBody PaymentRequest request) {
        String message = walletService.payCurrentMonthBill(request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // 1.10 Payer des factures spécifiques
    @PostMapping("/pay-factures")
    public ResponseEntity<Map<String, String>> payFactures(@Valid @RequestBody PayFacturesRequest request) {
        String message = walletService.paySpecificBills(request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // 1.11 Historique des transactions par téléphone
    @GetMapping("/{phoneNumber}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(walletService.getTransactionHistory(phoneNumber));
    }
}
