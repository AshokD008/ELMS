package com.lms.usermanagementservice.security.userdetails;

import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findWithRolesAndCollegeByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return toPrincipal(user);
    }

    @Transactional(readOnly = true)
    public CustomUserDetails loadUserById(Long userId) {

        User user = userRepository.findWithRolesAndCollegeById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with ID: " + userId
                        )
                );

        return toPrincipal(user);
    }

    private UserPrincipal toPrincipal(User user) {

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getEnabled()),
                Boolean.TRUE.equals(user.getAccountNonLocked()),
                user.getUserRoles()
                        .stream()
                        .map(userRole -> userRole.getRole().getRoleName())
                        .map(this::normalizeRole)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );
    }

    private String normalizeRole(String roleName) {

        return roleName != null && roleName.startsWith("ROLE_")
                ? roleName
                : "ROLE_" + roleName;
    }
}
