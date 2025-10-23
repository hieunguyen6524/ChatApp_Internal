package com.example.ChatApp_Internal.controller;

import com.example.ChatApp_Internal.dto.response.ApiResponse;
import com.example.ChatApp_Internal.dto.response.FileResponse;
import com.example.ChatApp_Internal.entity.FileMetadata;
import com.example.ChatApp_Internal.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FileController {

    private final AwsS3Service awsS3Service;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "general") String folder) {

        FileMetadata fileMetadata = awsS3Service.uploadFile(file, folder);

        FileResponse response = FileResponse.builder()
                .fileId(fileMetadata.getFileId())
                .originalFileName(fileMetadata.getOriginalFileName())
                .contentType(fileMetadata.getContentType())
                .fileSize(fileMetadata.getFileSize())
                .fileType(fileMetadata.getFileType())
                .url(fileMetadata.getUrl())
                .createdAt(fileMetadata.getCreatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        byte[] fileData = awsS3Service.downloadFile(fileId);

        // Get file metadata for headers
        // You might want to add a method to get metadata without downloading

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileData);
    }

    @GetMapping("/{fileId}/presigned-url")
    public ResponseEntity<ApiResponse<String>> generatePresignedUrl(
            @PathVariable Long fileId,
            @RequestParam(defaultValue = "60") int expirationMinutes) {

        String presignedUrl = awsS3Service.generatePresignedUrl(fileId, expirationMinutes);
        return ResponseEntity.ok(ApiResponse.success(presignedUrl));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long fileId) {
        awsS3Service.deleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }
}
