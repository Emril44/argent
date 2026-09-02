package org.example.transaction;

import org.example.money.Money;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestBody TransactionRequest request, @RequestHeader UUID userId) {
        Transaction newDeposit = transactionService.deposit(userId, request.getIdempotencyKey(), request.getWalletId(), new Money(request.getAmount()));
        TransactionResponse response = new TransactionResponse(newDeposit.getId(), newDeposit.getIdempotencyKey(), null, newDeposit.getDestinationWalletId().orElse(null), newDeposit.getAmount(), newDeposit.getTransactionType(), newDeposit.getTransactionStatus(), newDeposit.getCreatedAt(), newDeposit.getProcessedAt().orElse(null));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@RequestBody TransactionRequest request, @RequestHeader UUID userId) {
        Transaction newWithdraw = transactionService.withdraw(userId, request.getIdempotencyKey(), request.getWalletId(), new Money(request.getAmount()));
        TransactionResponse response = new TransactionResponse(newWithdraw.getId(), newWithdraw.getIdempotencyKey(), newWithdraw.getSourceWalletId().orElse(null), null, newWithdraw.getAmount(), newWithdraw.getTransactionType(), newWithdraw.getTransactionStatus(), newWithdraw.getCreatedAt(), newWithdraw.getProcessedAt().orElse(null));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody TransactionTransferRequest request, @RequestHeader UUID userId) {
        Transaction newTransfer = transactionService.transfer(userId, request.getIdempotencyKey(), request.getSourceId(), request.getDestinationId(), new Money(request.getAmount()));
        TransactionResponse response = new TransactionResponse(newTransfer.getId(), newTransfer.getIdempotencyKey(), newTransfer.getSourceWalletId().orElse(null), newTransfer.getDestinationWalletId().orElse(null), newTransfer.getAmount(), newTransfer.getTransactionType(), newTransfer.getTransactionStatus(), newTransfer.getCreatedAt(), newTransfer.getProcessedAt().orElse(null));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{transId}")
    public ResponseEntity<TransactionResponse> fetchTransaction(@PathVariable UUID transId) {
        Transaction foundTransaction = transactionService.fetchTransaction(transId);
        TransactionResponse response = new TransactionResponse(foundTransaction.getId(), foundTransaction.getIdempotencyKey(), foundTransaction.getSourceWalletId().orElse(null), foundTransaction.getDestinationWalletId().orElse(null), foundTransaction.getAmount(), foundTransaction.getTransactionType(), foundTransaction.getTransactionStatus(), foundTransaction.getCreatedAt(), foundTransaction.getProcessedAt().orElse(null));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
