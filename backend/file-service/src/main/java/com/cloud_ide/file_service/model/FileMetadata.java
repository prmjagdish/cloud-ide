package com.cloud_ide.file_service.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name="file_metadata")
public final class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID projectId;
    private String path;
    private Boolean isFolder;
    private Instant createdAt;
    private Instant updatedAt;
}
