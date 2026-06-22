package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.OAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import com.lms.usermanagementservice.enums.AuthProvider;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    Optional<OAuthAccount> findByProviderAndProviderUserId(
            String provider,
            String providerUserId
    );

    Optional<OAuthAccount> findByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );

    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<OAuthAccount> findByUserIdAndProvider(Long userId, AuthProvider provider);

    boolean existsByUserIdAndProvider(Long userId, AuthProvider provider);

    Optional<OAuthAccount> findByProviderEmail(String providerEmail);
}
