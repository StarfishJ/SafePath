package com.safepath.dto;

import java.time.LocalDateTime;

/**
 * Task status response DTO
 */
public class TaskStatusResponse {
    private String taskId;
    private TaskStatus status;
    private String message;
    private Integer progress; // 0-100
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;

    public enum TaskStatus {
        PENDING,    // Pending
        RUNNING,    // Running
        COMPLETED,  // Completed
        FAILED      // Failed
    }

    public TaskStatusResponse() {
    }

    public TaskStatusResponse(String taskId, TaskStatus status) {
        this.taskId = taskId;
        this.status = status;
        this.progress = 0;
        this.startTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

