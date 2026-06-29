package com.payment.service;

import com.payment.model.Facture;
import com.payment.model.ServiceType;
import com.payment.repository.FactureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;

    // Factures impayées du mois en cours
    public List<Facture> getUnpaidCurrentMonth(String walletCode, String unite) {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);

        if (unite != null && !unite.isBlank()) {
            ServiceType serviceType = ServiceType.valueOf(unite.toUpperCase());
            return factureRepository.findByWalletCodeAndPayeeFalseAndServiceNameAndMois(
                    walletCode, serviceType, firstDayOfMonth);
        }
        return factureRepository.findByWalletCodeAndPayeeFalseAndMois(walletCode, firstDayOfMonth);
    }

    // Factures impayées sur une période
    public List<Facture> getUnpaidByPeriod(String walletCode, String debut, String fin) {
        LocalDate dateDebut = LocalDate.parse(debut);
        LocalDate dateFin = LocalDate.parse(fin);
        return factureRepository.findByWalletCodeAndPayeeFalseAndMoisBetween(walletCode, dateDebut, dateFin);
    }

    // Payer la facture du mois en cours
    @Transactional
    public void payCurrentMonthBill(String walletCode, String serviceName, BigDecimal amount) {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        ServiceType serviceType = ServiceType.valueOf(serviceName.toUpperCase());

        Facture facture = factureRepository
                .findByWalletCodeAndServiceNameAndMoisAndPayeeFalse(walletCode, serviceType, firstDayOfMonth)
                .orElseThrow(() -> new RuntimeException(
                        "Aucune facture impayée du mois en cours pour " + walletCode + " - " + serviceName));

        facture.setPayee(true);
        facture.setPaidAt(LocalDateTime.now());
        factureRepository.save(facture);
    }

    // Calcul du total de factures spécifiques
    public BigDecimal getTotalForFactures(String walletCode, String serviceName, List<String> references) {
        List<Facture> factures = factureRepository.findByReferenceIn(references);
        if (factures.isEmpty()) {
            throw new RuntimeException("Aucune facture trouvée pour les références données");
        }
        // Vérifie que les factures appartiennent bien au wallet
        factures.forEach(f -> {
            if (!f.getWalletCode().equals(walletCode)) {
                throw new RuntimeException("Facture " + f.getReference() + " n'appartient pas au wallet " + walletCode);
            }
            if (f.isPayee()) {
                throw new RuntimeException("Facture " + f.getReference() + " est déjà payée");
            }
        });
        return factures.stream()
                .map(Facture::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Marquer des factures spécifiques comme payées
    @Transactional
    public void markFacturesAsPaid(String walletCode, String serviceName, List<String> references) {
        List<Facture> factures = factureRepository.findByReferenceIn(references);
        factures.forEach(f -> {
            f.setPayee(true);
            f.setPaidAt(LocalDateTime.now());
        });
        factureRepository.saveAll(factures);
    }
}
