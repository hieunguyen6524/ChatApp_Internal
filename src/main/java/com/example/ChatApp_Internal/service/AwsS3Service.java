package com.example.ChatApp_Internal.service;

import com.example.ChatApp_Internal.config.AwsS3Config;
import com.example.ChatApp_Internal.config.UploadConfig;
import com.example.ChatApp_Internal.entity.Account;
import com.example.ChatApp_Internal.entity.FileMetadata;
import com.example.ChatApp_Internal.repository.AccountRepository;
import com.example.ChatApp_Internal.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsS3Config awsConfig;
    private final UploadConfig uploadConfig;
    private final FileMetadataRepository fileMetadataRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public FileMetadata uploadFile(MultipartFile file, String folder) {
        validateFile(file);

        Account currentAccount = getCurrentAccount();

        // Generate S3 key
        String fileName = file.getOriginalFilename();
        String extension = getFileExtension(fileName);
        String s3Key = generateS3Key(folder, extension);

        try {
            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsConfig.getBucketName())
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Generate public URL
            String fileUrl = String.format("%s/%s", awsConfig.getBaseUrl(), s3Key);

            // Save metadata
            FileMetadata fileMetadata = FileMetadata.builder()
                    .account(currentAccount)
                    .s3Key(s3Key)
                    .originalFileName(fileName)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileType(determineFileType(file.getContentType()))
                    .url(fileUrl)
                    .isDeleted(false)
                    .build();

            fileMetadataRepository.save(fileMetadata);

            log.info("File uploaded successfully: {}", s3Key);
            return fileMetadata;

        } catch (IOException e) {
            log.error("Failed to upload file to S3: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    public byte[] downloadFile(Long fileId) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (fileMetadata.getIsDeleted()) {
            throw new RuntimeException("File has been deleted");
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(awsConfig.getBucketName())
                    .key(fileMetadata.getS3Key())
                    .build();

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();

        } catch (Exception e) {
            log.error("Failed to download file from S3: {}", e.getMessage());
            throw new RuntimeException("Failed to download file: " + e.getMessage());
        }
    }

    public String generatePresignedUrl(Long fileId, int expirationMinutes) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(fileMetadata.getS3Key())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }

    @Transactional
    public void deleteFile(Long fileId) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Account currentAccount = getCurrentAccount();

        // Check ownership
        if (!fileMetadata.getAccount().getAccountId().equals(currentAccount.getAccountId())) {
            throw new RuntimeException("You don't have permission to delete this file");
        }

        try {
            // Delete from S3
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(awsConfig.getBucketName())
                    .key(fileMetadata.getS3Key())
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            // Mark as deleted in database
            fileMetadata.setIsDeleted(true);
            fileMetadata.setDeletedAt(System.currentTimeMillis());
            fileMetadataRepository.save(fileMetadata);

            log.info("File deleted successfully: {}", fileMetadata.getS3Key());

        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > uploadConfig.getMaxFileSize()) {
            throw new RuntimeException("File size exceeds maximum limit of " +
                    (uploadConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!uploadConfig.getAllowedExtensionsList().contains(extension)) {
            throw new RuntimeException("File type not allowed: " + extension);
        }
    }

    private String generateS3Key(String folder, String extension) {
        String uuid = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        return String.format("%s/%d-%s.%s", folder, timestamp, uuid, extension);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String determineFileType(String contentType) {
        if (contentType == null) {
            return "OTHER";
        }

        if (contentType.startsWith("image/")) {
            return "IMAGE";
        } else if (contentType.startsWith("video/")) {
            return "VIDEO";
        } else if (contentType.equals("application/pdf")) {
            return "PDF";
        } else if (contentType.contains("word") || contentType.contains("document")) {
            return "DOCUMENT";
        } else if (contentType.contains("sheet") || contentType.contains("excel")) {
            return "SPREADSHEET";
        } else if (contentType.contains("zip") || contentType.contains("rar")) {
            return "ARCHIVE";
        }

        return "OTHER";
    }

    private Account getCurrentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

}
