package com.fiap.vp_upload.infra.adapter.output;

import com.fiap.vp_upload.application.ports.output.MessageOutput;
import com.fiap.vp_upload.domain.model.ProcessRequest;
import com.fiap.vp_upload.domain.model.UploadError;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageOutputImpl implements MessageOutput {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Gson gson;

    @Value("${kafka.producer.topic.processor}")
    private String processorTopic;

    @Value("${kafka.producer.topic.notification}")
    private String notificationTopic;

    @Override
    public void sendProcessMessage(ProcessRequest processRequest) {

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(processorTopic, gson.toJson(processRequest));
        log.info("Enviando mensagem para o tópico {}: {}", processorTopic, producerRecord.value());
        kafkaTemplate.send(producerRecord);
    }

    @Override
    public void sendFailMessage(UploadError uploadError) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(notificationTopic, gson.toJson(uploadError));
        log.info("Enviando mensagem de erro para o tópico {}: {}", notificationTopic, producerRecord.value());
        kafkaTemplate.send(producerRecord);
    }
}
