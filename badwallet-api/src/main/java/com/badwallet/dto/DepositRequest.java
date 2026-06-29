package com.badwallet.dto;

import com.badwallet.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal amount;

    @NotNull(message = "La méthode de paiement est obligatoire")
    private PaymentMethod paymentMethod;

    // Utilisé seulement si paymentMethod = WALLET_TARGET
    private String sourcePhone;
}
