package com.vic.gatewayservice.Controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class OpenApiAggregatorController {

    private final WebClient webClient = WebClient.create();

    // ✅ Default docs route → points to Wallet
    @GetMapping("/v3/api-docs")
    public Mono<String> defaultDocs() {
        return walletDocs(); // Reuse the walletDocs() method
    }

    @GetMapping("/v3/api-docs/wallet")
    public Mono<String> walletDocs() {
        return webClient.get()
                .uri("http://localhost:8080/v3/api-docs")
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/v3/api-docs/history")
    public Mono<String> historyDocs() {
        return webClient.get()
                .uri("http://localhost:8081/v3/api-docs")
                .retrieve()
                .bodyToMono(String.class);
    }
}
