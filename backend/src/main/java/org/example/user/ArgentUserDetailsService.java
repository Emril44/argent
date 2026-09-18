package org.example.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ArgentUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    public ArgentUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity foundEntity = userRepository.findByEmail(email);
        if (foundEntity == null) {
            throw new UsernameNotFoundException("User with email not found!");
        };

        return new ArgentUserDetails(foundEntity);
    }
}
