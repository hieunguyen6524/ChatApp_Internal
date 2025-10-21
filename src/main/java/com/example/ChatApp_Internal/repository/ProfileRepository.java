package com.example.ChatApp_Internal.repository;

import com.example.ChatApp_Internal.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUsername(String username);

    boolean existsByUsername(String username);
}
