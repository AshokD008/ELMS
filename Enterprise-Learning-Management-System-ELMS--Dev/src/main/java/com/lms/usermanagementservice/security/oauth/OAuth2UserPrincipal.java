package com.lms.usermanagementservice.security.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@AllArgsConstructor
public class OAuth2UserPrincipal implements OAuth2User {

    private final Long userId;

    private final String email;

    private final String name;

    private final Collection<? extends GrantedAuthority> authorities;

    private final Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {

        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return authorities;
    }

    @Override
    public String getName() {

        return name;
    }
}