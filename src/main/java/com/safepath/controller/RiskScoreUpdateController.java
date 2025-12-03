package com.safepath.controller;

import com.safepath.dto.TaskStatusResponse;
import com.safepath.service.RiskScoreUpdateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Risk score update controller
 * Provides an API for manually triggering the risk score update
 */
@RestController
@RequestMapping("/api/admin/risk-score")
@CrossOrigin(origins = {"http://localhost:9090", "http://127.0.0.1:9090"})
public class RiskScoreUpdateController {

    private final RiskScoreUpdateService updateService;

    public RiskScoreUpdateController(RiskScoreUpdateService updateService) {
        this.updateService = updateService;
    }

    /**
     * Trigger the risk score update
     * POST /api/admin/risk-score/update
     * 
     * @return task ID and initial status
     */
    @PostMapping("/update")
    public ResponseEntity<TaskStatusResponse> triggerUpdate() {
        String taskId = updateService.triggerUpdate();
        TaskStatusResponse status = updateService.getTaskStatus(taskId);
        return ResponseEntity.ok(status);
    }

    /**
     * Query the task status
     * GET /api/admin/risk-score/status/{taskId}
     * 
     * @param taskId task ID
     * @return task status
     */
    @GetMapping("/status/{taskId}")
    public ResponseEntity<TaskStatusResponse> getTaskStatus(@PathVariable String taskId) {
        TaskStatusResponse status = updateService.getTaskStatus(taskId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    /**
     * Get the latest task status
     * GET /api/admin/risk-score/status/latest
     * 
     * @return the latest task status
     */
    @GetMapping("/status/latest")
    public ResponseEntity<TaskStatusResponse> getLatestTaskStatus() {
        TaskStatusResponse status = updateService.getLatestTaskStatus();
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }
}

