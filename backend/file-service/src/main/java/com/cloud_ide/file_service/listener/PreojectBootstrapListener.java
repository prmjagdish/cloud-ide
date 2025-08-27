package com.cloud_ide.file_service.listener;

import com.cloud_ide.file_service.dto.ProjectBootstrapRequest;
import com.cloud_ide.file_service.service.impl.FileServiceImpl;
import com.cloud_ide.file_service.service.impl.ProjectBootstrapImpl;
import com.cloudide.common.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreojectBootstrapListener {

    private final ProjectBootstrapImpl projectBootstrap;

//    @RabbitListener(queues = RabbitMQConfig.PROJECT_BOOTSTRAP_QUEUE)
//    public void handleProjectBootstrap(ProjectBootstrapRequest request) {
//        System.out.println("Received event: " + request);
//        projectBootstrap.initializeProjectStructure(request.getProjectId());
//    }
}
