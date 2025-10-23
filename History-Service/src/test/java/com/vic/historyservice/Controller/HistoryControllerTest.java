package com.vic.historyservice.Controller;

import com.vic.historyservice.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Sql("/data.sql")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HistoryControllerTest extends BaseIntegrationTest {

    private static final String EXISTING_WALLET_ID = "8b7d98f8-562d-498e-a350-3b0969d5dc44";
    private static final String EXISTING_USER_ID = "userId234";
    private static final String NON_EXISTENT_WALLET_ID = "non-existent-wallet";
    private static final String NON_EXISTENT_USER_ID = "ghost1-user";
    private static final String SECOND_WALLET_ID = "wallet-abc-123";
    private static final String OTHER_USER_ID = "user567";

    @Test
    @Order(1)
    void shouldReturnWalletHistory_WhenWalletExists() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/wallets/{walletId}/history", EXISTING_WALLET_ID)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", not(empty()))
                .body("size()", greaterThanOrEqualTo(5))
                .body("[0].walletId", equalTo(EXISTING_WALLET_ID))
                .body("[0].event_type", notNullValue())
                .body("[0].amount", notNullValue())
                .body("[0].transactionType", notNullValue())
                .body("[0].createdAt", notNullValue())
                .body("findAll { it.amount > 0 }.size()", greaterThan(0))
                .body("event_type", hasItems("WALLET_CREATED", "WALLET_FUNDED", "TRANSFER_COMPLETED"));
    }

    @Test
    @Order(2)
    void shouldReturn404_WhenWalletDoesNotExist() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/wallets/{walletId}/history", NON_EXISTENT_WALLET_ID)
                .then()
                .statusCode(200);
    }

    @Test
    @Order(3)
    void shouldReturnUserActivityHistory_WhenUserExists() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/users/{userId}/activity", EXISTING_USER_ID)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", not(empty()))
                .body("size()", greaterThanOrEqualTo(8))
                .body("[0].userId", equalTo(EXISTING_USER_ID))
                .body("[0].event_type", notNullValue())
                .body("[0].amount", notNullValue())
                .body("[0].transactionType", notNullValue())
                .body("[0].createdAt", notNullValue())
                .body("findAll { it.walletId == '8b7d98f8-562d-498e-a350-3b0969d5dc44' }.size()", greaterThan(5))
                .body("findAll { it.walletId == 'wallet-abc-123' }.size()", greaterThan(1));
    }

    @Test
    @Order(4)
    void shouldReturn404_WhenUserDoesNotExist() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/users/{userId}/activity", NON_EXISTENT_USER_ID)
                .then()
                .statusCode(200);
    }

    @Test
    @Order(5)
    void shouldReturnWalletHistoryWithCorrectEventTypes() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/wallets/{walletId}/history", EXISTING_WALLET_ID)
                .then()
                .statusCode(200)
                .body("event_type", hasItems("WALLET_CREATED", "WALLET_FUNDED", "TRANSFER_COMPLETED", "WALLET_FUNDING_FAILED", "TRANSFER_FAILED"));
    }

    @Test
    @Order(6)
    void shouldReturnEmptyList_WhenNoHistoryExistsForValidWallet() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/wallets/{walletId}/history", "brand-new-wallet")
                .then()
                .statusCode(200)
                .body("$", empty());
    }

    @Test
    @Order(7)
    void shouldReturnHistoryForSecondWallet() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/wallets/{walletId}/history", SECOND_WALLET_ID)
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].walletId", equalTo(SECOND_WALLET_ID))
                .body("event_type", hasItems("WALLET_CREATED", "WALLET_FUNDED"))
                .body("findAll { it.event_type == 'WALLET_FUNDED' }[0].amount", equalTo(500.0f));
    }

    @Test
    @Order(8)
    void shouldReturnHistoryForOtherUser() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/users/{userId}/activity", OTHER_USER_ID)
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].userId", equalTo(OTHER_USER_ID))
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @Order(9)
    void shouldReturnFailedTransactionsInHistory() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/wallets/{walletId}/history", EXISTING_WALLET_ID)
                .then()
                .statusCode(200)
                .body("findAll { it.event_type.endsWith('FAILED') }.size()", greaterThan(0))
                .body("findAll { it.event_type == 'WALLET_FUNDING_FAILED' }[0].amount", equalTo(200.0f))
                .body("findAll { it.event_type == 'TRANSFER_FAILED' }[0].amount", equalTo(75.0f));
    }

    @Test
    @Order(10)
    void shouldReturnTransferCompletedEventsWithDirectionInEventData() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/wallets/{walletId}/history", EXISTING_WALLET_ID)
                .then()
                .statusCode(200)
                .body("findAll { it.event_type == 'TRANSFER_COMPLETED' }.size()", greaterThan(0));
    }

    @Test
    @Order(11)
    void shouldHaveCorrectEventTypeDistribution() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/wallets/{walletId}/history", EXISTING_WALLET_ID)
                .then()
                .statusCode(200)
                .body("findAll { it.event_type == 'WALLET_CREATED' }.size()", equalTo(1))
                .body("findAll { it.event_type == 'WALLET_FUNDED' }.size()", equalTo(2))
                .body("findAll { it.event_type == 'TRANSFER_COMPLETED' }.size()", equalTo(4))
                .body("findAll { it.event_type == 'WALLET_FUNDING_FAILED' }.size()", equalTo(1))
                .body("findAll { it.event_type == 'TRANSFER_FAILED' }.size()", equalTo(1));
    }
}