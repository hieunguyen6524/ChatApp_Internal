package com.example.ChatApp_Internal.security;

import com.example.ChatApp_Internal.entity.Account;
import com.example.ChatApp_Internal.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        if (!account.getIsActive()) {
            throw new RuntimeException("Account is inactive");
        }

        List<GrantedAuthority> authorities = account.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());

        return User.builder()
                .username(account.getEmail())
                .password(account.getPassword() != null ? account.getPassword() : "")
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!account.getIsActive())
                .credentialsExpired(false)
                .disabled(!account.getIsActive())
                .build();
    }
}
