package com.vic.historyservice.Controller;

import com.vic.historyservice.Models.TransactionEvents;
import com.vic.historyservice.Service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "History Controller", description = "Endpoints for retrieving wallet and user transaction histories")
@RestController
@RequestMapping("/api")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @Operation(
            summary = "Get wallet transaction history",
            description = "Fetches all transaction events for a specific wallet ID, including deposits, withdrawals, and transfers.",
            parameters = @Parameter(name = "walletId", description = "The wallet's unique identifier", required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TransactionEvents.class),
                                    examples = @ExampleObject(value = """
                                            [
                                              {
                                                "id": "tx-event-001",
                                                "walletId": "wallet-123",
                                                "user_id": "user-456",
                                                "amount": 100.00,
                                                "event_type": "DEPOSIT",
                                                "transaction_id": "tx-111",
                                                "createdAt": "2025-10-09T15:32:00",
                                                "eventData":
                                                 "{
                                                "walletId": "wallet-123",
                                                "user_id": "user-456",
                                                "amount": 100.00,
                                                "event_type": "DEPOSIT",
                                                "transaction_id": "tx-111",
                                                "createdAt": "2025-10-09T15:32:00",
                                                 }"
                                              },
                                              {
                                                "id": "tx-event-002",
                                                "walletId": "wallet-123",
                                                "user_id": "user-456",
                                                "amount": 25.00,
                                                "event_type": "TRANSFER_OUT",
                                                "transaction_id": "tx-222",
                                                "createdAt": "2025-10-09T16:45:00",
                                                "eventData": "{
                                            
                                                 "walletId": "wallet-123",
                                                "user_id": "user-456",
                                                "amount": 25.00,
                                                "event_type": "TRANSFER_OUT",
                                                "transaction_id": "tx-222",
                                                "createdAt": "2025-10-09T16:45:00",
                                            
                                                 }"
                                              }
                                            ]
                                            """))),
                    @ApiResponse(responseCode = "404", description = "Wallet not found", content = @Content)
            }
    )
    @GetMapping("/wallets/{walletId}/history")
    public ResponseEntity<List<TransactionEvents>> getWalletHistory(@PathVariable String walletId) {
        return ResponseEntity.ok(historyService.getWalletHistory(walletId));
    }

    @Operation(
            summary = "Get user activity history",
            description = "Retrieves all transaction events across all wallets belonging to a user.",
            parameters = @Parameter(name = "userId", description = "The user's unique identifier", required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User activity history retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TransactionEvents.class),
                                    examples = @ExampleObject(value = """
                                            [
                                              {
                                                "id": "tx-event-010",
                                                "walletId": "wallet-001",
                                                "user_id": "user-456",
                                                "amount": 50.00,
                                                "event_type": "TRANSFER_IN",
                                                "transaction_id": "tx-333",
                                                "createdAt": "2025-10-09T18:00:00",
                                                "eventData": "{
                                            
                                                 "walletId": "wallet-001",
                                                "user_id": "user-456",
                                                "amount": 50.00,
                                                "event_type": "TRANSFER_IN",
                                                "transaction_id": "tx-333",
                                                "createdAt": "2025-10-09T18:00:00",
                                            
                                                 }"
                                              },
                                              {
                                                "id": "tx-event-011",
                                                "walletId": "wallet-002",
                                                "user_id": "user-456",
                                                "amount": 10.00,
                                                "event_type": "WITHDRAWAL",
                                                "transaction_id": "tx-444",
                                                "createdAt": "2025-10-09T18:30:00",
                                                "eventData": "{
                                            
                                                 "walletId": "wallet-002",
                                                "user_id": "user-456",
                                                "amount": 10.00,
                                                "event_type": "WITHDRAWAL",
                                                "transaction_id": "tx-444",
                                                "createdAt": "2025-10-09T18:30:00",
                                            
                                                 }"
                                              }
                                            ]
                                            """))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @GetMapping("/users/{userId}/activity")
    public ResponseEntity<List<TransactionEvents>> getUserActivityHistory(@PathVariable String userId) {
        return ResponseEntity.ok(historyService.getUserHistory(userId));
    }
}
