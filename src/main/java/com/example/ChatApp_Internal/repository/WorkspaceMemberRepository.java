package com.example.ChatApp_Internal.repository;

import com.example.ChatApp_Internal.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    Optional<WorkspaceMember> findByWorkspaceWorkspaceIdAndAccountAccountId(Long workspaceId, Long accountId);

    List<WorkspaceMember> findByWorkspaceWorkspaceIdAndIsActiveTrue(Long workspaceId);

    boolean existsByWorkspaceWorkspaceIdAndAccountAccountId(Long workspaceId, Long accountId);
}
