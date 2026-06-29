package com.badwallet.seeder;

import com.badwallet.model.*;
import com.badwallet.repository.TransactionRepository;
import com.badwallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletSeederService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    private static final String[] CURRENCIES = {"XOF"};
    private static final TransactionType[] TRANSACTION_TYPES = {
            TransactionType.DEPOSIT, TransactionType.WITHDRAWAL,
            TransactionType.PAYMENT_ISM, TransactionType.PAYMENT_WOYAFAL
    };

    @Async
    @Transactional
    public void seedAsync(int numWallets, int eventsPerWallet) {
        log.info("Démarrage du seeding : {} wallets, {} events chacun", numWallets, eventsPerWallet);
        Random random = new Random();

        for (int i = 1; i <= numWallets; i++) {
            String phone = "+22177000000" + String.format("%01d", i);
            String code = "WLT-" + String.format("%07d", i);

            if (walletRepository.existsByPhoneNumber(phone)) {
                log.info("Wallet {} existe déjà, on passe", phone);
                continue;
            }

            Wallet wallet = Wallet.builder()
                    .phoneNumber(phone)
                    .email("client" + i + "@badwallet.sn")
                    .balance(new BigDecimal(10000 + random.nextInt(490000)))
                    .code(code)
                    .currency("XOF")
                    .build();

            walletRepository.save(wallet);

            for (int j = 0; j < eventsPerWallet; j++) {
                TransactionType type = TRANSACTION_TYPES[random.nextInt(TRANSACTION_TYPES.length)];
                BigDecimal amount = new BigDecimal(500 + random.nextInt(49500));

                Transaction transaction = Transaction.builder()
                        .wallet(wallet)
                        .amount(amount)
                        .type(type)
                        .description("Event auto-généré #" + (j + 1))
                        .fees(BigDecimal.ZERO)
                        .build();
                transactionRepository.save(transaction);
            }

            log.info("Wallet {} créé avec {} transactions", phone, eventsPerWallet);
        }

        log.info("Seeding terminé ✓");
    }
}
