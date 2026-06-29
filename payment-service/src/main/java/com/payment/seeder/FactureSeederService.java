package com.payment.seeder;

import com.payment.model.Facture;
import com.payment.model.ServiceType;
import com.payment.repository.FactureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactureSeederService {

    private final FactureRepository factureRepository;

    // Montant mensuel ISM : 75 000 XOF, WOYAFAL : variable
    private static final BigDecimal ISM_MONTHLY = new BigDecimal("75000");

    @Async
    @Transactional
    public void seedAsync(int numWallets) {
        log.info("Démarrage seeding factures pour {} wallets", numWallets);

        for (int i = 1; i <= numWallets; i++) {
            String walletCode = "WLT-" + String.format("%07d", i);

            // Génère 6 mois de factures (3 mois passés + mois courant + 2 mois futurs)
            LocalDate startMonth = LocalDate.now().minusMonths(3).withDayOfMonth(1);

            for (int m = 0; m < 6; m++) {
                LocalDate mois = startMonth.plusMonths(m);

                // Facture ISM
                String ismRef = "FAC-ISM-" + i + "-" + (m + 1);
                if (factureRepository.findByReference(ismRef).isEmpty()) {
                    Facture ismFacture = Facture.builder()
                            .reference(ismRef)
                            .walletCode(walletCode)
                            .serviceName(ServiceType.ISM)
                            .montant(ISM_MONTHLY)
                            .mois(mois)
                            .payee(m < 2) // Les 2 premiers mois déjà payés
                            .build();
                    factureRepository.save(ismFacture);
                }

                // Facture WOYAFAL (montant variable selon consommation simulée)
                String woyafalRef = "FAC-WOYAFAL-" + i + "-" + (m + 1);
                if (factureRepository.findByReference(woyafalRef).isEmpty()) {
                    BigDecimal woyafalAmount = new BigDecimal(5000 + (i * 300) % 25000);
                    Facture woyafalFacture = Facture.builder()
                            .reference(woyafalRef)
                            .walletCode(walletCode)
                            .serviceName(ServiceType.WOYAFAL)
                            .montant(woyafalAmount)
                            .mois(mois)
                            .payee(m < 2)
                            .build();
                    factureRepository.save(woyafalFacture);
                }
            }

            log.info("Factures générées pour {}", walletCode);
        }

        log.info("Seeding factures terminé ✓");
    }
}
