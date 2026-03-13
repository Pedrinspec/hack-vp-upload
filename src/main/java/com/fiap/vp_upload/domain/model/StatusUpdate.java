package com.fiap.vp_upload.domain.model;

import com.fiap.vp_upload.infra.adapter.output.repository.entities.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StatusUpdate {
    private UUID uploadId;
    private StatusEnum status;
}
