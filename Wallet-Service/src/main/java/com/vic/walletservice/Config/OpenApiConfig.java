package com.vic.walletservice.Config;

import org.springframework.beans.factory.annotation.Value;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * OpenApi documentation configuration
 */

@Configuration
public class OpenApiConfig {


    @Value("${serverUrl}")
    private String serverUrl;

    @Bean
    public OpenAPI walletServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Wallet Transaction System API")
                        .version("v1.0")
                        .description("""
                                **Digital Wallet Transaction System** — a simple and secure digital wallet
                                platform for managing funds, transferring money, sending notifications and tracking user activities.
                                """)
                )
                .servers(List.of(
                        new Server().url(serverUrl).description("Local Development Server")
                ));
    }
}
