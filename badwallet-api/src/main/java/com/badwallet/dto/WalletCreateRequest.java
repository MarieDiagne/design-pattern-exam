package com.badwallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletCreateRequest {

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String phoneNumber;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotNull(message = "Le solde initial est obligatoire")
    @Positive(message = "Le solde initial doit être positif")
    private BigDecimal initialBalance;

    @NotBlank(message = "Le code est obligatoire")
    private String code;

    @NotBlank(message = "La devise est obligatoire")
    private String currency;
}
