package com.cloud_ide.project_service.messaging.publisher;

import com.cloud_ide.project_service.dto.ProjectBootstrapRequest;
import com.cloud_ide.project_service.dto.ProjectDeleteRequest;

import com.cloud_ide.project_service.messaging.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void bootstrapProject(ProjectBootstrapRequest request) {
        // Send to RabbitMQ Exchange
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PROJECT_EXCHANGE,
                RabbitMQConfig.PROJECT_BOOTSTRAP_KEY,
                request);

        System.out.println(" [x] Sent Project Bootstrap Event for projectId=" + request.getProjectId());
    }

//    public void renameProject(ProjectRenameRequest request) {
//        rabbitTemplate.convertAndSend(
//                RabbitMQConfig.PROJECT_EXCHANGE,
//                RabbitMQConfig.PROJECT_RENAME_KEY,
//                request
//        );
//    }

    public void deleteProject(ProjectDeleteRequest request) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PROJECT_EXCHANGE,
                RabbitMQConfig.PROJECT_DELETE_KEY,
                request.getProjectId()
        );
    }
}
