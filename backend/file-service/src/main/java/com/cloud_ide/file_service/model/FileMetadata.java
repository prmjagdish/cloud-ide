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

    // Existing columns
    private UUID projectId;
    private String path;
    private Boolean isFolder;
    private Instant createdAt;
    private Instant updatedAt;

    // NEW COLUMNS FOR EXACT FRONTEND FORMAT
    private String nodeId;          // Simple IDs: 'src', 'App.js', 'package.json'
    private String name;            // Display names
    private String parentNodeId;    // Parent reference (null for root children)
    private Integer sortOrder;      // Order within parent
}
