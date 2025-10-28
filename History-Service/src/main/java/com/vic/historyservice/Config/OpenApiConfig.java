package com.vic.historyservice.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${serverUrl}")
    private String serverUrl;

    @Bean
    public OpenAPI historyServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vic Wallet Transaction History API")
                        .version("v1.0")
                        .description("""
                                The **Vic History Service API** provides access to wallet and user transaction histories.
                                It allows querying all events linked to wallets and users for auditing, analytics,
                                and transaction tracking purposes.
                                """)
                )
                .servers(List.of(
                        new Server().url(serverUrl).description("Local Development Server")
                ));
    }
}
