package org.example.notification;

import org.example.transaction.TransactionCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TransactionEventListener {
    Logger logger = LoggerFactory.getLogger(TransactionEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        logger.info("USER [{}] executes TRANSACTION [{}]; RESULT: [{}]", event.getUserId(), event.getTransactionId(), event.getStatus());
    }
}
