package com.safepath.service;

import com.safepath.dto.TaskStatusResponse;

/**
 * Risk score update service interface
 */
public interface RiskScoreUpdateService {
    
    /**
     * Trigger the risk score update task (asynchronously)
     * @return task ID, if there is a task running, return null
     */
    String triggerUpdate();
    
    /**
     * Force trigger the update (even if there is a task running)
     * @return task ID
     */
    String triggerUpdateForce();
    
    /**
     * Check if there is a task running
     * @return true if there is a task in PENDING or RUNNING state
     */
    boolean hasRunningTask();
    
    /**
     * Query the task status
     * @param taskId task ID
     * @return task status
     */
    TaskStatusResponse getTaskStatus(String taskId);
    
    /**
     * Get the latest task status
     * @return the latest task status, if there is no task, return null
     */
    TaskStatusResponse getLatestTaskStatus();
    
    /**
     * Get the last successful update time
     * @return the last successful update time, if there is no update, return null
     */
    java.time.LocalDateTime getLastSuccessfulUpdateTime();
}

