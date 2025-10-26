package com.example.ChatApp_Internal.repository;

import com.example.ChatApp_Internal.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findByIsArchivedFalse();

    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.account.accountId = :accountId AND m.isActive = true AND w.isArchived = false")
    List<Workspace> findByMemberAccountId(Long accountId);
}
