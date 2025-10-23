package com.example.ChatApp_Internal.repository;

import com.example.ChatApp_Internal.entity.Account;
import com.example.ChatApp_Internal.entity.AuthProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Account> findAll(Pageable pageable);

    @Query("SELECT a FROM Account a WHERE a.isActive = true")
    List<Account> findAllActiveAccounts();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.isActive = true")
    long countActiveAccounts();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.provider = :provider")
    long countByProvider(AuthProvider provider);
}
