package com.badwallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotBlank(message = "Le numéro de l'expéditeur est obligatoire")
    private String senderPhone;

    @NotBlank(message = "Le numéro du destinataire est obligatoire")
    private String receiverPhone;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal amount;
}
