package org.example.transaction;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findBySourceId(UUID walletId);
    List<TransactionEntity> findByDestinationId(UUID walletId);
    Optional<TransactionEntity> findByIdempotencyKey(UUID idempotencyKey);
}
