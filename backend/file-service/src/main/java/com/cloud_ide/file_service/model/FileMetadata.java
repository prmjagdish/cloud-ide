package com.cloud_ide.file_service.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name="file_matadata")
public final class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;
    private String path;
    private Boolean isFolder;
    private Instant createdAt;
    private Instant updatedAt;
}
