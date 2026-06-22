package com.lms.usermanagementservice.security.userdetails;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class UserPrincipal extends CustomUserDetails {

    public UserPrincipal(
            Long userId,
            String email,
            String password,
            boolean enabled,
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(
                userId,
                email,
                password,
                enabled,
                accountNonLocked,
                authorities
        );
    }

    public Long getUserId() {
        return getId();
    }

    public String getEmail() {
        return getUsername();
    }
}
