package com.badwallet.dto;

import com.badwallet.model.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private String referencePhone;
    private BigDecimal fees;
    private LocalDateTime createdAt;
}
