package com.cloud_ide.project_service.service.impl;

import com.cloud_ide.project_service.dto.ProjectBootstrapRequest;
import com.cloud_ide.project_service.dto.ProjectDeleteRequest;
import com.cloud_ide.project_service.dto.ProjectRenameRequest;
import com.cloud_ide.project_service.service.TaskService;
import com.cloudide.common.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectTaskServiceImpl implements TaskService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void bootstrapProject(ProjectBootstrapRequest request) {
        // Send to RabbitMQ Exchange
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PROJECT_EXCHANGE,
                RabbitMQConfig.PROJECT_BOOTSTRAP_KEY,
                request.getProjectId()   // serailse nathi thathu full dto
        );
        System.out.println(" [x] Sent Project Bootstrap Event for projectId=" + request.getProjectId());
    }

    @Override
    public void renameProject(ProjectRenameRequest request) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PROJECT_EXCHANGE,
                RabbitMQConfig.PROJECT_RENAME_KEY,
                request
        );
    }

    @Override
    public void deleteProject(ProjectDeleteRequest request) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PROJECT_EXCHANGE,
                RabbitMQConfig.PROJECT_DELETE_KEY,
                request
        );
    }
}
