package com.safepath.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.safepath.service.RiskScoreUpdateService;

/**
 * Listener for automatically updating the risk score on application startup
 * Asynchronously trigger the risk score update after the application is fully started, without blocking the application startup
 */
@Component
public class RiskScoreAutoUpdateListener {

    private static final Logger logger = LoggerFactory.getLogger(RiskScoreAutoUpdateListener.class);

    private final RiskScoreUpdateService updateService;

    @Value("${safepath.risk-score.auto-update.enabled:true}")
    private boolean autoUpdateEnabled;

    @Value("${safepath.risk-score.auto-update.delay-seconds:5}")
    private int delaySeconds;
    
    @Value("${safepath.risk-score.auto-update.min-interval-hours:1}")
    private int minIntervalHours;

    public RiskScoreAutoUpdateListener(RiskScoreUpdateService updateService) {
        this.updateService = updateService;
    }

    /**
     * Listen for the application startup completion event
     * Trigger the risk score update after the application is fully started
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!autoUpdateEnabled) {
            logger.info("Risk score update is disabled (safepath.risk-score.auto-update.enabled=false)");
            return;
        }

        logger.info("Application startup completed, will trigger the risk score update in {} seconds...", delaySeconds);

        // delay execution, ensure the application is fully ready
        new Thread(() -> {
            try {
                Thread.sleep(delaySeconds * 1000L);
                
                // check if there is a task running
                if (updateService.hasRunningTask()) {
                    logger.info("There is a task running, skip the automatic update");
                    return;
                }
                
                // check the time interval since the last successful update
                java.time.LocalDateTime lastUpdate = updateService.getLastSuccessfulUpdateTime();
                if (lastUpdate != null) {
                    java.time.Duration duration = java.time.Duration.between(lastUpdate, java.time.LocalDateTime.now());
                    long hoursSinceLastUpdate = duration.toHours();
                    
                    if (hoursSinceLastUpdate < minIntervalHours) {
                        logger.info("Less than {} hours since the last successful update, skip the automatic update", 
                            hoursSinceLastUpdate, minIntervalHours);
                        return;
                    }
                }
                
                logger.info("Starting the risk score update...");
                String taskId = updateService.triggerUpdate();
                
                if (taskId == null) {
                    logger.warn("Update failed, possibly a task is running");
                } else {
                    logger.info("Risk score update task created, task ID: {}", taskId);
                    logger.info("Update will be executed asynchronously in the background, without affecting the application normal use");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Automatic update interrupted");
            } catch (Exception e) {
                logger.error("Failed to trigger the risk score update", e);
            }
        }, "RiskScoreAutoUpdate").start();
    }
}

