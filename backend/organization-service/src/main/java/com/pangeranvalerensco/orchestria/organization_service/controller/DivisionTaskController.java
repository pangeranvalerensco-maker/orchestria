package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.TaskStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.DivisionTaskRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.DivisionTaskResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.DivisionTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization/division-tasks")
@RequiredArgsConstructor
public class DivisionTaskController {

    private final DivisionTaskService taskService;

    @GetMapping
    @PreAuthorize("hasAuthority('division.task.read')")
    public ResponseEntity<ApiResponse<List<DivisionTaskResponse>>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('division.task.read')")
    public ResponseEntity<ApiResponse<DivisionTaskResponse>> getTaskById(
            @PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping("/division/{divisionId}")
    @PreAuthorize("hasAuthority('division.task.read')")
    public ResponseEntity<ApiResponse<List<DivisionTaskResponse>>> getTasksByDivision(
            @PathVariable Long divisionId) {
        return ResponseEntity.ok(taskService.getTasksByDivision(divisionId));
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAuthority('division.task.read')")
    public ResponseEntity<ApiResponse<List<DivisionTaskResponse>>> getTasksByAssignedMember(
            @PathVariable Long memberId) {
        return ResponseEntity.ok(taskService.getTasksByAssignedMember(memberId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('division.task.read')")
    public ResponseEntity<ApiResponse<List<DivisionTaskResponse>>> getTasksByStatus(
            @PathVariable TaskStatus status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('division.task.manage')")
    public ResponseEntity<ApiResponse<DivisionTaskResponse>> createTask(
            @Valid @RequestBody DivisionTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('division.task.manage')")
    public ResponseEntity<ApiResponse<DivisionTaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody DivisionTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @PatchMapping("/{id}/status/{status}")
    @PreAuthorize("hasAuthority('division.task.manage')")
    public ResponseEntity<ApiResponse<DivisionTaskResponse>> updateTaskStatus(
            @PathVariable Long id,
            @PathVariable TaskStatus status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('division.task.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long id) {
        return ResponseEntity.ok(taskService.deleteTask(id));
    }
}