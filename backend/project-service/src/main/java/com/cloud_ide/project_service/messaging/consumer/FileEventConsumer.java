package com.cloud_ide.project_service.messaging.consumer;

import com.cloud_ide.project_service.dto.ProjectReadyEvent;
import com.cloud_ide.project_service.service.impl.ProjectServiceImpl;
import com.cloudide.common.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class FileEventConsumer {

    private final ProjectServiceImpl projectService;

    public FileEventConsumer(ProjectServiceImpl projectService) {
        this.projectService = projectService;
    }

    @RabbitListener(queues = RabbitMQConfig.PROJECT_READY_QUEUE)
    public void handleProjectReady(ProjectReadyEvent event) {
        System.out.println(" [x] Received Project Ready Event for projectId=" + event.getProjectId());
        projectService.updateStatus(event);
    }
}
