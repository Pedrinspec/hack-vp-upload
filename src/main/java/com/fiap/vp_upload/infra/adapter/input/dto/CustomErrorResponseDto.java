package com.fiap.vp_upload.infra.adapter.input.dto;

import lombok.Data;

@Data
public class CustomErrorResponseDto {
    private int statusCode;
    private String message;
    private String description;
}
