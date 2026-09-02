package org.example.wallet;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@RestController
@RequestMapping(value = "/wallets")
public class WalletController {
    private final WalletService walletService;

    WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/")
    public ResponseEntity<WalletResponse> createWallet(@RequestBody WalletRequest request) {
        Wallet newWallet = walletService.createWallet(request.getUserId());
        WalletResponse walletResponse = new WalletResponse(newWallet.getId(), newWallet.getOwner().getId(), newWallet.getBalance(), newWallet.getStatus(), newWallet.getCreatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(walletResponse);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> fetchWalletDetails(@PathVariable UUID walletId, @RequestHeader UUID userId) {
        try {
            Wallet foundWallet = walletService.getWallet(walletId, userId);
            WalletResponse walletResponse = new WalletResponse(foundWallet.getId(), foundWallet.getOwner().getId(), foundWallet.getBalance(), foundWallet.getStatus(), foundWallet.getCreatedAt());
            return ResponseEntity.status(HttpStatus.OK).body(walletResponse);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PatchMapping("/{walletId}/freeze")
    public ResponseEntity<WalletResponse> freezeWallet(@PathVariable UUID walletId, @RequestHeader UUID userId) {
        try {
            Wallet freezeWallet = walletService.freezeWallet(walletId, userId);
            WalletResponse walletResponse = new WalletResponse(freezeWallet.getId(), freezeWallet.getOwner().getId(), freezeWallet.getBalance(), freezeWallet.getStatus(), freezeWallet.getCreatedAt());
            return ResponseEntity.status(HttpStatus.OK).body(walletResponse);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PatchMapping("/{walletId}/unfreeze")
    public ResponseEntity<WalletResponse> unfreezeWallet(@PathVariable UUID walletId, @RequestHeader UUID userId) {
        try {
            Wallet unfreezeWallet = walletService.unfreezeWallet(walletId, userId);
            WalletResponse walletResponse = new WalletResponse(unfreezeWallet.getId(), unfreezeWallet.getOwner().getId(), unfreezeWallet.getBalance(), unfreezeWallet.getStatus(), unfreezeWallet.getCreatedAt());
            return ResponseEntity.status(HttpStatus.OK).body(walletResponse);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
