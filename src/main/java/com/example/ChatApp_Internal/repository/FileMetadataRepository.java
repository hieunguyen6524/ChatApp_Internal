package com.example.ChatApp_Internal.repository;

import com.example.ChatApp_Internal.entity.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByS3Key(String s3Key);

    Page<FileMetadata> findByAccountAccountIdAndIsDeletedFalse(Long accountId, Pageable pageable);

    List<FileMetadata> findByAccountAccountIdAndFileTypeAndIsDeletedFalse(Long accountId, String fileType);

    @Query("SELECT SUM(f.fileSize) FROM FileMetadata f WHERE f.account.accountId = :accountId AND f.isDeleted = false")
    Long getTotalFileSizeByAccountId(Long accountId);
}