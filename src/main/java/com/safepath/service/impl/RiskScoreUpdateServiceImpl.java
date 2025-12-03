package com.safepath.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.safepath.dto.TaskStatusResponse;
import com.safepath.service.RiskScoreUpdateService;

/**
 * Risk score update service implementation
 * Asynchronously execute the Python script to update the segment risk score
 */
@Service
public class RiskScoreUpdateServiceImpl implements RiskScoreUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(RiskScoreUpdateServiceImpl.class);
    
    // store the task status (in actual project, can use Redis or database)
    private final ConcurrentMap<String, TaskStatusResponse> taskStatusMap = new ConcurrentHashMap<>();
    
    @Value("${safepath.python.script.path:data-process-insert/segment_risk_clustering.py}")
    private String pythonScriptPath;
    
    @Value("${safepath.python.executable:python}")
    private String pythonExecutable;
    
    @Value("${safepath.project.root:}")
    private String projectRoot;
    
    // record the last successful update time
    private LocalDateTime lastSuccessfulUpdateTime = null;

    @Override
    public String triggerUpdate() {
        // check if there is a task running
        if (hasRunningTask()) {
            logger.warn("There is a task running, skip this update request");
            return null;
        }
        
        return triggerUpdateForce();
    }
    
    @Override
    public String triggerUpdateForce() {
        String taskId = UUID.randomUUID().toString();
        TaskStatusResponse status = new TaskStatusResponse(taskId, TaskStatusResponse.TaskStatus.PENDING);
        status.setMessage("Task created, waiting to be executed...");
        taskStatusMap.put(taskId, status);
        
        logger.info("Trigger the risk score update task: {}", taskId);
        
        // asynchronously execute
        executeUpdateAsync(taskId);
        
        return taskId;
    }
    
    @Override
    public boolean hasRunningTask() {
        return taskStatusMap.values().stream()
            .anyMatch(task -> 
                task.getStatus() == TaskStatusResponse.TaskStatus.PENDING ||
                task.getStatus() == TaskStatusResponse.TaskStatus.RUNNING
            );
    }
    
    @Override
    public LocalDateTime getLastSuccessfulUpdateTime() {
        return lastSuccessfulUpdateTime;
    }

    @Async
    public void executeUpdateAsync(String taskId) {
        TaskStatusResponse status = taskStatusMap.get(taskId);
        if (status == null) {
            logger.error("Task not found: {}", taskId);
            return;
        }
        
        status.setStatus(TaskStatusResponse.TaskStatus.RUNNING);
        status.setMessage("Executing Python script...");
        status.setProgress(10);
        status.setStartTime(LocalDateTime.now());
        
        logger.info("Executing the risk score update task: {}", taskId);
        
        try {
            // build the Python command
            String scriptPath = getScriptPath();
            ProcessBuilder processBuilder = new ProcessBuilder(
                pythonExecutable,
                scriptPath
            );
            
            // set the working directory to the project root
            if (projectRoot != null && !projectRoot.isEmpty()) {
                processBuilder.directory(new File(projectRoot));
            } else {
                // default to use the parent directory of the script directory
                File scriptFile = new File(scriptPath);
                processBuilder.directory(scriptFile.getParentFile().getParentFile());
            }
            
            // set the environment variables (Python script will read db.properties)
            processBuilder.environment().put("PYTHONUNBUFFERED", "1");
            
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // read the output (for progress tracking)
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            int progress = 10;
            StringBuilder output = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.info("[Python] {}", line);
                
                // simple progress update (can parse more precise progress based on the script output)
                if (progress < 90) {
                    progress += 5;
                    status.setProgress(progress);
                    status.setMessage("Processing: " + line);
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                status.setStatus(TaskStatusResponse.TaskStatus.COMPLETED);
                status.setMessage("Risk score update completed!");
                status.setProgress(100);
                status.setEndTime(LocalDateTime.now());
                // record the last successful update time
                lastSuccessfulUpdateTime = LocalDateTime.now();
                logger.info("Risk score update task completed: {}", taskId);
            } else {
                status.setStatus(TaskStatusResponse.TaskStatus.FAILED);
                status.setMessage("Script execution failed, exit code: " + exitCode);
                status.setErrorMessage(output.toString());
                status.setEndTime(LocalDateTime.now());
                logger.error("Risk score update task failed: {}, exit code: {}", taskId, exitCode);
            }
            
        } catch (Exception e) {
            status.setStatus(TaskStatusResponse.TaskStatus.FAILED);
            status.setMessage("Execution error: " + e.getMessage());
            status.setErrorMessage(e.getClass().getName() + ": " + e.getMessage());
            status.setEndTime(LocalDateTime.now());
            logger.error("Error executing the risk score update task: {}", taskId, e);
        }
    }

    @Override
    public TaskStatusResponse getTaskStatus(String taskId) {
        return taskStatusMap.get(taskId);
    }

    @Override
    public TaskStatusResponse getLatestTaskStatus() {
        return taskStatusMap.values().stream()
            .max((a, b) -> {
                if (a.getStartTime() == null && b.getStartTime() == null) return 0;
                if (a.getStartTime() == null) return -1;
                if (b.getStartTime() == null) return 1;
                return a.getStartTime().compareTo(b.getStartTime());
            })
            .orElse(null);
    }
    
    private String getScriptPath() {
        // if the absolute path is configured, use it directly
        if (new File(pythonScriptPath).isAbsolute()) {
            return pythonScriptPath;
        }
        
        // otherwise relative to the project root
        if (projectRoot != null && !projectRoot.isEmpty()) {
            return new File(projectRoot, pythonScriptPath).getAbsolutePath();
        }
        
        // default: assume the script is in the data-process-insert directory
        // here needs to adjust according to the actual project structure
        File scriptFile = new File(pythonScriptPath);
        if (scriptFile.exists()) {
            return scriptFile.getAbsolutePath();
        }
        
        // try to find from the current working directory
        return pythonScriptPath;
    }
}

