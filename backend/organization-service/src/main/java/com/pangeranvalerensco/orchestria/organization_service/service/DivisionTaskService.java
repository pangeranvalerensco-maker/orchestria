package com.pangeranvalerensco.orchestria.organization_service.service;

import java.util.List;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.TaskStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.DivisionTaskRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.DivisionTaskResponse;

public interface DivisionTaskService {

    ApiResponse<List<DivisionTaskResponse>> getAllTasks();

    ApiResponse<List<DivisionTaskResponse>> getTasksByDivision(Long divisionId);

    ApiResponse<List<DivisionTaskResponse>> getTasksByAssignedMember(Long memberId);

    ApiResponse<List<DivisionTaskResponse>> getTasksByStatus(TaskStatus status);

    ApiResponse<DivisionTaskResponse> getTaskById(Long id);

    ApiResponse<DivisionTaskResponse> createTask(DivisionTaskRequest request);

    ApiResponse<DivisionTaskResponse> updateTask(Long id, DivisionTaskRequest request);

    ApiResponse<DivisionTaskResponse> updateTaskStatus(Long id, TaskStatus status);

    ApiResponse<Void> deleteTask(Long id);

    ApiResponse<List<DivisionTaskResponse>> getMyTasks();

    ApiResponse<DivisionTaskResponse> updateMyTaskStatus(Long id, TaskStatus status);
}
