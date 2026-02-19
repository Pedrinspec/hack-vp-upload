package com.fiap.vp_upload.infra.adapter.output.repository.entities;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadPart {
    @Column(name = "part_number", nullable = false)
    private int partNumber;

    @Column(name = "etag", nullable = false)
    private String eTag;
}
