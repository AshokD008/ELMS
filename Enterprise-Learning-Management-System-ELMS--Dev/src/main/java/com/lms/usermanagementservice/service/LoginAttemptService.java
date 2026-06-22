package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        int failures = user.getFailedLoginAttempts() == null ? 1 : user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failures);
        if (failures >= 5) {
            user.setLockoutUntil(LocalDateTime.now().plusMinutes(15));
            user.setAccountLocked(true);
        }
        userRepository.save(user);
    }
}
