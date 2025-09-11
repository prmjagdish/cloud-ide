package com.cloud_ide.executor_service.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

@Slf4j
@Service
public class TempCleaner {

    /**
     * Delete temporary project directory after container stops
     *
     * @param projectId unique project identifier
     */
    public void cleanup(String projectId) {
        Path tmpDir = Paths.get("/tmp", projectId);
        try {
            if (Files.exists(tmpDir)) {
                Files.walk(tmpDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);

                log.info("🧹 Cleaned project files for {}", projectId);
            }
        } catch (IOException e) {
            log.error("❌ Failed to clean project files for {}: {}", projectId, e.getMessage());
        }
    }

}
