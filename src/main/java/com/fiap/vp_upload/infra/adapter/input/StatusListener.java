package com.fiap.vp_upload.infra.adapter.input;

import com.fiap.vp_upload.application.usecase.UploadUseCase;
import com.fiap.vp_upload.domain.model.StatusUpdate;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatusListener {
    private final Gson gson;
    private final UploadUseCase uploadUseCase;

    @KafkaListener(groupId = "${kafka.consumer.group-id}", topics = {"${kafka.consumer.topic}"}, containerFactory = "customKafkaTemplate")
    public void statusListener(String uploadStatusJson) {
        log.info("Mensagem recebida: {}", uploadStatusJson);
        StatusUpdate statusUpdate = gson.fromJson(uploadStatusJson, StatusUpdate.class);
        log.info("json convertido para StatusUpdate: {}", statusUpdate);
        uploadUseCase.updateStatus(statusUpdate);
    }
}
