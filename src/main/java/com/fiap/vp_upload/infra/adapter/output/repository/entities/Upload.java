package com.fiap.vp_upload.infra.adapter.output.repository.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "upload")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Upload {
    @Id
    @Column(name = "upload_id", nullable = false, unique = true)
    private UUID uploadId;

    @Column(name = "s3_upload_id", nullable = false)
    private String s3UploadId;

    @Column(name = "file_key", nullable = false)
    private String key;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "status_id", nullable = false)
    private String status;
}
