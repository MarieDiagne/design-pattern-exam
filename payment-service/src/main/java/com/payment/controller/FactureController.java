package com.payment.controller;

import com.payment.model.Facture;
import com.payment.service.FactureService;
import com.payment.seeder.FactureSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;
    private final FactureSeederService factureSeederService;

    // Seeder les factures
    @PostMapping("/seed")
    public ResponseEntity<Map<String, String>> seed(
            @RequestParam(defaultValue = "10") int numWallets) {
        factureSeederService.seedAsync(numWallets);
        return ResponseEntity.accepted().body(Map.of("status", "Seeding factures en cours"));
    }

    // Factures impayées du mois en cours (avec ou sans filtre unite)
    @GetMapping("/{walletCode}/current")
    public ResponseEntity<List<Facture>> getCurrentUnpaid(
            @PathVariable String walletCode,
            @RequestParam(required = false) String unite) {
        return ResponseEntity.ok(factureService.getUnpaidCurrentMonth(walletCode, unite));
    }

    // Factures impayées sur une période
    @GetMapping("/{walletCode}/periode")
    public ResponseEntity<List<Facture>> getByPeriod(
            @PathVariable String walletCode,
            @RequestParam String debut,
            @RequestParam String fin) {
        return ResponseEntity.ok(factureService.getUnpaidByPeriod(walletCode, debut, fin));
    }

    // Payer la facture du mois en cours (appelé par badwallet-api)
    @PostMapping("/pay-current")
    public ResponseEntity<Void> payCurrentMonth(@RequestBody Map<String, Object> body) {
        String walletCode = (String) body.get("walletCode");
        String serviceName = (String) body.get("serviceName");
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        factureService.payCurrentMonthBill(walletCode, serviceName, amount);
        return ResponseEntity.ok().build();
    }

    // Calculer le total des factures spécifiques
    @PostMapping("/total")
    public ResponseEntity<Map<String, Object>> getTotal(@RequestBody Map<String, Object> body) {
        String walletCode = (String) body.get("walletCode");
        String serviceName = (String) body.get("serviceName");
        @SuppressWarnings("unchecked")
        List<String> references = (List<String>) body.get("references");
        BigDecimal total = factureService.getTotalForFactures(walletCode, serviceName, references);
        return ResponseEntity.ok(Map.of("total", total));
    }

    // Marquer des factures comme payées
    @PostMapping("/pay-specific")
    public ResponseEntity<Void> paySpecific(@RequestBody Map<String, Object> body) {
        String walletCode = (String) body.get("walletCode");
        String serviceName = (String) body.get("serviceName");
        @SuppressWarnings("unchecked")
        List<String> references = (List<String>) body.get("references");
        factureService.markFacturesAsPaid(walletCode, serviceName, references);
        return ResponseEntity.ok().build();
    }
}
