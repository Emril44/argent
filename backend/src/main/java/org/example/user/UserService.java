package org.example.user;

import jakarta.transaction.Transactional;
import org.example.exceptions.DuplicateEmailException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public User register(String name, String email, String passwordHash) {
        if(userRepository.findByEmail(email) != null) {
            throw new DuplicateEmailException("Found user connected to " + email + ". Please try another email.");
        }

        User newUser = new User(name, email, passwordHash);
        UserEntity newEntity = newUser.mapUserToEntity();
        userRepository.save(newEntity);

        // TODO: log returning new user
        eventPublisher.publishEvent(new UserRegisteredEvent(name, email, UserStatus.UNVERIFIED));
        return newUser;
    }
}
