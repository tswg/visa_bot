package com.example.visaslotbot.service;

import com.example.visaslotbot.model.User;
import com.example.visaslotbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getOrCreateUser(Long telegramId, String username) {
        return userRepository.findByTelegramId(telegramId)
                .map(existing -> updateUsernameIfNeeded(existing, username))
                .orElseGet(() -> userRepository.save(User.builder()
                        .telegramId(telegramId)
                        .username(username)
                        .createdAt(Instant.now())
                        .blocked(false)
                        .build()));
    }

    private User updateUsernameIfNeeded(User user, String username) {
        if (username != null && !username.equals(user.getUsername())) {
            user.setUsername(username);
            return userRepository.save(user);
        }
        return user;
    }

    public void markBlocked(User user) {
        user.setBlocked(true);
        userRepository.save(user);
    }
}
