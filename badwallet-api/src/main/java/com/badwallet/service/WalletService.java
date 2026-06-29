package com.badwallet.service;

import com.badwallet.dto.*;
import com.badwallet.model.*;
import com.badwallet.repository.TransactionRepository;
import com.badwallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentClientService paymentClientService;

    // Frais retrait : 1% plafonné à 5000
    private static final BigDecimal WITHDRAWAL_FEE_RATE = new BigDecimal("0.01");
    private static final BigDecimal WITHDRAWAL_FEE_CAP = new BigDecimal("5000");

    // ==================== CRÉATION ====================

    @Transactional
    public WalletResponse createWallet(WalletCreateRequest request) {
        if (walletRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Un portefeuille avec ce numéro existe déjà : " + request.getPhoneNumber());
        }
        if (walletRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un portefeuille avec cet email existe déjà : " + request.getEmail());
        }
        if (walletRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Un portefeuille avec ce code existe déjà : " + request.getCode());
        }

        Wallet wallet = Wallet.builder()
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .balance(request.getInitialBalance())
                .code(request.getCode())
                .currency(request.getCurrency())
                .build();

        Wallet saved = walletRepository.save(wallet);
        return toResponse(saved);
    }

    // ==================== LISTING ====================

    public Page<WalletResponse> listWallets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return walletRepository.findAll(pageable).map(this::toResponse);
    }

    // ==================== CONSULTATION ====================

    public WalletResponse getWalletByPhone(String phoneNumber) {
        Wallet wallet = findByPhone(phoneNumber);
        return toResponse(wallet);
    }

    public BigDecimal getBalance(String phoneNumber) {
        Wallet wallet = findByPhone(phoneNumber);
        return wallet.getBalance();
    }

    // ==================== DÉPÔT ====================

    @Transactional
    public WalletResponse deposit(Long walletId, DepositRequest request) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Portefeuille introuvable avec l'id : " + walletId));

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .description("Dépôt via " + request.getPaymentMethod())
                .fees(BigDecimal.ZERO)
                .build();
        transactionRepository.save(transaction);

        return toResponse(wallet);
    }

    // ==================== RETRAIT ====================

    @Transactional
    public WalletResponse withdraw(WithdrawRequest request) {
        Wallet wallet = findByPhone(request.getPhoneNumber());

        BigDecimal amount = request.getAmount();
        BigDecimal fees = amount.multiply(WITHDRAWAL_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        if (fees.compareTo(WITHDRAWAL_FEE_CAP) > 0) {
            fees = WITHDRAWAL_FEE_CAP;
        }

        BigDecimal totalDeducted = amount.add(fees);
        if (wallet.getBalance().compareTo(totalDeducted) < 0) {
            throw new RuntimeException("Solde insuffisant. Solde actuel : " + wallet.getBalance() + ", Total nécessaire : " + totalDeducted);
        }

        wallet.setBalance(wallet.getBalance().subtract(totalDeducted));
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(TransactionType.WITHDRAWAL)
                .description("Retrait - frais : " + fees + " XOF")
                .fees(fees)
                .build();
        transactionRepository.save(transaction);

        return toResponse(wallet);
    }

    // ==================== TRANSFERT ====================

    @Transactional
    public String transfer(TransferRequest request) {
        Wallet sender = findByPhone(request.getSenderPhone());
        Wallet receiver = findByPhone(request.getReceiverPhone());

        BigDecimal amount = request.getAmount();
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Solde insuffisant pour le transfert");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        walletRepository.save(sender);
        walletRepository.save(receiver);

        // Transaction expéditeur
        transactionRepository.save(Transaction.builder()
                .wallet(sender)
                .amount(amount)
                .type(TransactionType.TRANSFER_SENT)
                .description("Transfert vers " + request.getReceiverPhone())
                .referencePhone(request.getReceiverPhone())
                .fees(BigDecimal.ZERO)
                .build());

        // Transaction destinataire
        transactionRepository.save(Transaction.builder()
                .wallet(receiver)
                .amount(amount)
                .type(TransactionType.TRANSFER_RECEIVED)
                .description("Transfert reçu de " + request.getSenderPhone())
                .referencePhone(request.getSenderPhone())
                .fees(BigDecimal.ZERO)
                .build());

        return "Transfert de " + amount + " XOF effectué avec succès de " + request.getSenderPhone() + " vers " + request.getReceiverPhone();
    }

    // ==================== PAIEMENT FACTURE DU MOIS ====================

    @Transactional
    public String payCurrentMonthBill(PaymentRequest request) {
        Wallet wallet = findByPhone(request.getPhoneNumber());

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Solde insuffisant pour le paiement");
        }

        // Appel au payment-service pour valider la facture
        paymentClientService.payCurrentMonthBill(wallet.getCode(), request.getServiceName(), request.getAmount());

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);

        TransactionType type = request.getServiceName().equalsIgnoreCase("ISM")
                ? TransactionType.PAYMENT_ISM
                : TransactionType.PAYMENT_WOYAFAL;

        transactionRepository.save(Transaction.builder()
                .wallet(wallet)
                .amount(request.getAmount())
                .type(type)
                .description("Paiement " + request.getServiceName() + " mois en cours")
                .fees(BigDecimal.ZERO)
                .build());

        return "Paiement " + request.getServiceName() + " de " + request.getAmount() + " XOF effectué avec succès";
    }

    // ==================== PAIEMENT FACTURES SPÉCIFIQUES ====================

    @Transactional
    public String paySpecificBills(PayFacturesRequest request) {
        Wallet wallet = findByPhone(request.getPhoneNumber());

        // Récupère le montant total depuis payment-service
        BigDecimal totalAmount = paymentClientService.getTotalForFactures(
                wallet.getCode(), request.getServiceName(), request.getFactureReferences());

        if (wallet.getBalance().compareTo(totalAmount) < 0) {
            throw new RuntimeException("Solde insuffisant pour payer les factures");
        }

        // Marque les factures comme payées dans payment-service
        paymentClientService.markFacturesAsPaid(wallet.getCode(), request.getServiceName(), request.getFactureReferences());

        wallet.setBalance(wallet.getBalance().subtract(totalAmount));
        walletRepository.save(wallet);

        TransactionType type = request.getServiceName().equalsIgnoreCase("ISM")
                ? TransactionType.PAYMENT_ISM
                : TransactionType.PAYMENT_WOYAFAL;

        transactionRepository.save(Transaction.builder()
                .wallet(wallet)
                .amount(totalAmount)
                .type(type)
                .description("Paiement factures " + request.getServiceName() + " : " + String.join(", ", request.getFactureReferences()))
                .fees(BigDecimal.ZERO)
                .build());

        return "Paiement de " + request.getFactureReferences().size() + " factures " + request.getServiceName() + " pour un total de " + totalAmount + " XOF";
    }

    // ==================== HISTORIQUE ====================

    public List<TransactionResponse> getTransactionHistory(String phoneNumber) {
        Wallet wallet = findByPhone(phoneNumber);
        return transactionRepository.findByWalletOrderByCreatedAtDesc(wallet)
                .stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    // ==================== UTILITAIRES ====================

    private Wallet findByPhone(String phoneNumber) {
        return walletRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Portefeuille introuvable : " + phoneNumber));
    }

    private WalletResponse toResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .code(wallet.getCode())
                .phoneNumber(wallet.getPhoneNumber())
                .email(wallet.getEmail())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private TransactionResponse toTransactionResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .description(t.getDescription())
                .referencePhone(t.getReferencePhone())
                .fees(t.getFees())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
