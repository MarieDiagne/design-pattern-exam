package com.payment.repository;

import com.payment.model.Facture;
import com.payment.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByWalletCodeAndPayeeFalseAndMois(String walletCode, LocalDate mois);

    List<Facture> findByWalletCodeAndPayeeFalseAndServiceNameAndMois(
            String walletCode, ServiceType serviceName, LocalDate mois);

    List<Facture> findByWalletCodeAndPayeeFalseAndMoisBetween(
            String walletCode, LocalDate debut, LocalDate fin);

    Optional<Facture> findByReference(String reference);

    List<Facture> findByReferenceIn(List<String> references);

    Optional<Facture> findByWalletCodeAndServiceNameAndMoisAndPayeeFalse(
            String walletCode, ServiceType serviceName, LocalDate mois);
}
