package com.badwallet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentClientService {

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    private WebClient getClient() {
        return WebClient.builder().baseUrl(paymentServiceUrl).build();
    }

    public void payCurrentMonthBill(String walletCode, String serviceName, BigDecimal amount) {
        Map<String, Object> body = new HashMap<>();
        body.put("walletCode", walletCode);
        body.put("serviceName", serviceName);
        body.put("amount", amount);

        getClient().post()
                .uri("/api/factures/pay-current")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public BigDecimal getTotalForFactures(String walletCode, String serviceName, List<String> references) {
        Map<String, Object> body = new HashMap<>();
        body.put("walletCode", walletCode);
        body.put("serviceName", serviceName);
        body.put("references", references);

        Map response = getClient().post()
                .uri("/api/factures/total")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("total")) {
            throw new RuntimeException("Impossible de récupérer le total des factures");
        }

        return new BigDecimal(response.get("total").toString());
    }

    public void markFacturesAsPaid(String walletCode, String serviceName, List<String> references) {
        Map<String, Object> body = new HashMap<>();
        body.put("walletCode", walletCode);
        body.put("serviceName", serviceName);
        body.put("references", references);

        getClient().post()
                .uri("/api/factures/pay-specific")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public Object getUnpaidCurrentMonth(String walletCode, String unite) {
        String uri = "/api/factures/" + walletCode + "/current";
        if (unite != null && !unite.isBlank()) {
            uri += "?unite=" + unite;
        }
        return getClient().get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    public Object getUnpaidByPeriod(String walletCode, String debut, String fin) {
        String uri = "/api/factures/" + walletCode + "/periode?debut=" + debut + "&fin=" + fin;
        return getClient().get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }
}
