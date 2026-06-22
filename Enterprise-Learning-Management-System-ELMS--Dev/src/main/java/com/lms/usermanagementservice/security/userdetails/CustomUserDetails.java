package com.lms.usermanagementservice.security.userdetails;

import com.lms.usermanagementservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private Long id;

    private String username;

    private String password;

    private boolean enabled;

    private boolean accountNonLocked;

    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user, List<String> roles) {

        this.id = user.getId();

        this.username = user.getEmail();

        this.password = user.getPassword();

        this.enabled = Boolean.TRUE.equals(user.getEnabled());

        this.accountNonLocked = Boolean.TRUE.equals(user.getAccountNonLocked());

      
        		this.authorities = roles.stream()
                .map(this::normalizeRole)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        		

    }

    
    
    private String normalizeRole(String role) {

        return role != null && role.startsWith("ROLE_")
                ? role
                : "ROLE_" + role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return authorities;
    }
    

    @Override
    public String getPassword() {

        return password;
    }

    @Override
    public String getUsername() {

        return username;
    }

    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }

    @Override
    public boolean isEnabled() {

        return enabled;
    }
}

