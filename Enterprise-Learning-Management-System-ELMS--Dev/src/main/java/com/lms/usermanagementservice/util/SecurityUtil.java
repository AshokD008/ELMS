package com.lms.usermanagementservice.util;

import com.lms.usermanagementservice.entity.Role;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.entity.UserRole;
import com.lms.usermanagementservice.security.userdetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SecurityUtil {

    public static Authentication getAuthentication() {

        return SecurityContextHolder.getContext()
                .getAuthentication();
    }

    public static String getCurrentUsername() {

        Authentication authentication = getAuthentication();

        if (authentication == null) {
            return null;
        }

        return authentication.getName();
    }

    public static Long getCurrentUserId() {

        Authentication authentication = getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                instanceof CustomUserDetails userDetails)) {

            return null;
        }

        return userDetails.getId();
    }

    public static User getCurrentUser() {

        Authentication authentication = getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                instanceof CustomUserDetails userDetails)) {

            return null;
        }

        User user = new User();
        user.setId(userDetails.getId());
        user.setEmail(userDetails.getUsername());
        user.setUsername(userDetails.getUsername());

        List<UserRole> userRoles = new ArrayList<>();

        userDetails.getAuthorities().forEach(authority -> {
            Role role = new Role();
            role.setRoleName(authority.getAuthority());

            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);

            userRoles.add(userRole);
        });

        user.setUserRoles(userRoles);

        return user;
    }

    public static boolean isAuthenticated() {

        Authentication authentication = getAuthentication();

        return authentication != null
                && authentication.isAuthenticated();
    }
}
