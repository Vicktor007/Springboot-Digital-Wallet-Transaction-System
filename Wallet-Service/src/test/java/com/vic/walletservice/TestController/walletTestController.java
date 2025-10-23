package com.vic.walletservice.TestController;

import com.vic.walletservice.AbstractIT;
import com.vic.walletservice.Dtos.FundWalletRequest;
import com.vic.walletservice.Dtos.TransferRequest;
import com.vic.walletservice.Enums.TransactionStatus;
import com.vic.walletservice.Models.Wallet;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class walletTestController extends AbstractIT {

    String userId1 = "user1234";
    String userId2 = "user234";

    @Nested
    class CreateWalletTest {

        @Test
        void shouldCreateAndUseWallets() {
            // Setting up all mock behaviors
            when(walletService.createWallet(userId1)).thenReturn("wallet-user1234");

            when(walletService.createWallet(userId2)).thenReturn("wallet-user234");

            when(walletService.fundWallet(anyString(), anyString(), ArgumentMatchers.any()))
                    .thenReturn(TransactionStatus.COMPLETED);

            when(walletService.transferFunds(anyString(), anyString(), anyString(), ArgumentMatchers.any()))
                    .thenReturn(TransactionStatus.COMPLETED);

            when(walletService.getBalance(anyString())).thenReturn("100.00");
            when(walletService.getUserWallets(anyString())).thenReturn(List.of());

            // Testing wallet creation
            String walletA = createWallet(userId1);
            String walletB = createWallet(userId2);

            assertThat(walletA).isEqualTo("wallet-user1234");
            assertThat(walletB).isEqualTo("wallet-user234");

            // Testing funding
            TransactionStatus fundResult = shouldFundWallet(walletA, userId1, new BigDecimal("50.00"));
            assertThat(fundResult).isEqualTo(TransactionStatus.COMPLETED);

            // Testing transfer
            TransactionStatus transferResult = shouldTransferFundsBetweenWallet(walletA, userId1, walletB, new BigDecimal("25.00"));
            assertThat(transferResult).isEqualTo(TransactionStatus.COMPLETED);

            // Testing balance
            String balance = shouldGetWalletBalance(walletA);
            assertThat(balance).isEqualTo("100.00");

            // Testing user wallets
            List<Wallet> wallets = shouldGetUserWallets(userId1);
            assertThat(wallets).isEmpty(); // Since i am mocking empty list
        }



        private String createWallet(String userId) {
            return given().contentType(ContentType.JSON)
                    .pathParam("userId", userId)
                    .when()
                    .post("/wallets/{userId}", userId)
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .asString();
        }

        private TransactionStatus shouldFundWallet(String walletId, String userId, BigDecimal amount) {

            FundWalletRequest fundRequest = new FundWalletRequest(userId, amount);


            return given()
                    .contentType(ContentType.URLENC)
                    .pathParam("walletId", walletId)
                    .formParam("userId", fundRequest.userId())
                    .formParam("amount", fundRequest.amount())
                    .when()
                    .post("/wallets/{walletId}/fund")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .as(TransactionStatus.class);
        }

        private TransactionStatus shouldTransferFundsBetweenWallet(String walletId, String fromUserId, String toWalletId, BigDecimal amount)
        {
            TransferRequest transferRequest = new TransferRequest(fromUserId, toWalletId, amount);
            return given()
                    .contentType(ContentType.URLENC)
                    .pathParam("walletId", walletId)
                    .formParam("fromUserId", transferRequest.fromUserId())
                    .formParam("toWalletId", transferRequest.toWalletId())
                    .formParam("amount", transferRequest.amount())
                    .when()
                    .post("/wallets/{walletId}/transfer")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .as(TransactionStatus.class);
        }

        private String shouldGetWalletBalance(String walletId) {
            return given()
                    .contentType(ContentType.JSON)
                    .pathParam("walletId", walletId)
                    .when()
                    .get("/wallets/{walletId}")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .asString();
        }

        private List<Wallet> shouldGetUserWallets(String userId) {
            return given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", userId)
                    .when()
                    .get("/users/{userId}/wallets")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .jsonPath()
                    .getList(".", Wallet.class);
        }
    }


}