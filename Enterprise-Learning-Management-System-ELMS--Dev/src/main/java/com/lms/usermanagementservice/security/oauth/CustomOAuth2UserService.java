package com.lms.usermanagementservice.security.oauth;

import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
                new DefaultOAuth2UserService();

        OAuth2User oauth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");

        String name = (String) attributes.get("name");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new OAuth2AuthenticationException(
                                "User not found with email: " + email
                        ));

        return new OAuth2UserPrincipal(
                user.getId(),
                email,
                name,
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_USER")
                ),
                attributes
        );
    }
}