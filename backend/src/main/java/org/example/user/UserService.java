package org.example.user;

import jakarta.transaction.Transactional;
import org.example.exceptions.DuplicateEmailException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ApplicationEventPublisher eventPublisher, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String name, String email, String password) {
        if(userRepository.findByEmail(email) != null) {
            throw new DuplicateEmailException("Found user connected to " + email + ". Please try another email.");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(name, email, hashedPassword);
        UserEntity newEntity = newUser.mapUserToEntity();
        userRepository.save(newEntity);

        // TODO: log returning new user
        eventPublisher.publishEvent(new UserRegisteredEvent(name, email, UserStatus.UNVERIFIED));
        return newUser;
    }
}
