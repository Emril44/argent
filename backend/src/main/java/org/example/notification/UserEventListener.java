package org.example.notification;

import org.example.transaction.TransactionCompletedEvent;
import org.example.user.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserEventListener {
    Logger logger = LoggerFactory.getLogger(TransactionEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        logger.info("USER [{}] (connected EMAIL [{}]) REGISTERS; STATUS: [{}]", event.getUsername(), event.getEmail(), event.getStatus());
    }
}
