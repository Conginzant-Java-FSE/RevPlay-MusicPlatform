package com.revplay.revplay.security;

import com.revplay.revplay.entity.User;
import com.revplay.revplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + email));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Account is deactivated: " + email);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .accountExpired(false)
                .accountLocked(!user.isActive())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }

    @Component
    public static class SecurityPrincipal {

        public Optional<String> getCurrentUserEmail() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return Optional.empty();
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails ud) return Optional.of(ud.getUsername());
            return Optional.empty();
        }

        public String requireCurrentUserEmail() {
            return getCurrentUserEmail()
                    .orElseThrow(() ->
                            new IllegalStateException("No authenticated user in context"));
        }

        public boolean hasRole(String role) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return false;
            return auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
        }

        public boolean isAdmin()  { return hasRole("ADMIN");  }
        public boolean isArtist() { return hasRole("ARTIST"); }
    }
}