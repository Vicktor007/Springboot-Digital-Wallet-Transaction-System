package com.vic.walletservice;

import org.springframework.boot.SpringApplication;



public class TestWalletServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(WalletServiceApplication::main)
                .run(args);
    }

}
