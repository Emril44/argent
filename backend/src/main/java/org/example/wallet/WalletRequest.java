package org.example.wallet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class WalletRequest {
    private final UUID userId;

    @JsonCreator
    public WalletRequest(@JsonProperty("userId") UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
