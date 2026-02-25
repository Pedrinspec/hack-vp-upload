package com.fiap.vp_upload.infra.adapter.output;

import com.fiap.vp_upload.application.ports.output.MessageOutput;
import com.fiap.vp_upload.domain.model.ProcessRequest;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageOutputImpl implements MessageOutput {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Gson gson;

    @Value("${kafka.producer.topic}")
    private String topic;

    @Override
    public void sendProcessMessage(ProcessRequest processRequest) {

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, gson.toJson(processRequest));
        kafkaTemplate.send(producerRecord);
    }
}
