package com.badwallet.controller;

import com.badwallet.service.PaymentClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/external/factures")
@RequiredArgsConstructor
public class ExternalFacturesController {

    private final PaymentClientService paymentClientService;

    // 2.2 Factures impayées du mois en cours
    @GetMapping("/{walletCode}/current")
    public ResponseEntity<Object> getCurrentUnpaidBills(
            @PathVariable String walletCode,
            @RequestParam(required = false) String unite) {
        Object result = paymentClientService.getUnpaidCurrentMonth(walletCode, unite);
        return ResponseEntity.ok(result);
    }

    // 2.4 Factures impayées sur une période
    @GetMapping("/{walletCode}/periode")
    public ResponseEntity<Object> getBillsByPeriod(
            @PathVariable String walletCode,
            @RequestParam String debut,
            @RequestParam String fin) {
        Object result = paymentClientService.getUnpaidByPeriod(walletCode, debut, fin);
        return ResponseEntity.ok(result);
    }
}
