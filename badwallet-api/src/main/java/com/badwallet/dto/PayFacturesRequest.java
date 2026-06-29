package com.badwallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PayFacturesRequest {

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String phoneNumber;

    @NotBlank(message = "Le service est obligatoire (ISM ou WOYAFAL)")
    private String serviceName;

    @NotEmpty(message = "La liste des références de factures est obligatoire")
    private List<String> factureReferences;
}
