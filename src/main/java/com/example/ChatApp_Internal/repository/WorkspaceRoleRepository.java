package com.example.ChatApp_Internal.repository;

import com.example.ChatApp_Internal.entity.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceRoleRepository extends JpaRepository<WorkspaceRole, Integer> {
    Optional<WorkspaceRole> findByRoleName(String roleName);
}
