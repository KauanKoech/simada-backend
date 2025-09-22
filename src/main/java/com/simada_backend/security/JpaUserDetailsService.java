package com.simada_backend.security;

import com.simada_backend.model.User;
import com.simada_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {
    private final UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // mapear seu user para UserDetails simples
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPassword())
                .roles(mapRole(u.getUserType()))
                .build();
    }

    private String mapRole(String userType) {
        if (userType == null) return "user";
        String t = userType.trim();
        if (t.contains("coach")) return "coach";
        if (t.contains("athlete")) return "athlete";
        return "user";
    }
}
