package org.example.wallet;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {
    List<WalletEntity> findByOwnerId(UUID ownerId);

    @Query("SELECT w FROM WalletEntity w WHERE w.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    WalletEntity findByIdWithLock(UUID id);

    @Query("SELECT w.id FROM WalletEntity w WHERE w.id = :walletId AND w.owner.id = :ownerId")
    Optional<UUID> findUserWalletId(UUID walletId, UUID ownerId);
}