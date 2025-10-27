package com.example.ChatApp_Internal.controller;

import com.example.ChatApp_Internal.dto.request.AddMemberRequest;
import com.example.ChatApp_Internal.dto.request.CreateWorkspaceRequest;
import com.example.ChatApp_Internal.dto.response.ApiResponse;
import com.example.ChatApp_Internal.dto.response.MemberResponse;
import com.example.ChatApp_Internal.dto.response.WorkspaceResponse;
import com.example.ChatApp_Internal.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) {
        WorkspaceResponse workspaceResponse = workspaceService.createWorkspace(request);
        return ResponseEntity.ok((ApiResponse.success(workspaceResponse)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getMyWorkspaces() {
        List<WorkspaceResponse> workspaces = workspaceService.getMyWorkspaces();
        return ResponseEntity.ok(ApiResponse.success(workspaces));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspace(@PathVariable Long id) {
        WorkspaceResponse workspaceResponse = workspaceService.getWorkspaceById(id);
        return ResponseEntity.ok(ApiResponse.success(workspaceResponse));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable Long id,
            @RequestBody CreateWorkspaceRequest request) {
        WorkspaceResponse workspaceResponse = workspaceService.updateWorkspace(id, request);
        return ResponseEntity.ok(ApiResponse.success(workspaceResponse));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest request) {
        MemberResponse memberResponse = workspaceService.addMember(id, request);
        return ResponseEntity.ok(ApiResponse.success(memberResponse));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMembers(@PathVariable Long id) {
        List<MemberResponse> members = workspaceService.getWorkspaceMembers(id);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        workspaceService.removeMember(id, memberId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveWorkspace(@PathVariable Long id) {
        workspaceService.leaveWorkspace(id);
        return ResponseEntity.ok(ApiResponse.success("Left workspace successfully", null));
    }
}
