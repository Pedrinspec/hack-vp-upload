package com.fiap.vp_upload.infra.adapter.output;

import com.fiap.vp_upload.application.ports.output.UploadOutput;
import com.fiap.vp_upload.domain.exceptions.NoSuchChunkException;
import com.fiap.vp_upload.domain.exceptions.UploadNotExistException;
import com.fiap.vp_upload.domain.model.FinishUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class UploadAdapter implements UploadOutput {

    private static final String TEMP_DIR = "uploads";

    //TODO: Alterar salvamento de chunk para S3
    @Override
    public void uploadChunk(UUID uploadId, int chunkIndex, MultipartFile file) {
        Path uploadPath = Paths.get(TEMP_DIR, uploadId.toString());
        try {
            Files.createDirectories(uploadPath);
            Path chunkPath = uploadPath.resolve("chunk_" + chunkIndex + ".part");
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, chunkPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    //TODO: Mover geração de vídeo para worker
    @Override
    public void finishUpload(FinishUpload finishUpload) {
        String uploadId = finishUpload.getUploadId().toString();
        int totalChunks = finishUpload.getTotalChunks();
        String finishUploadOriginalFileName = finishUpload.getOriginalFileName();
        String originalFileName = Paths.get(finishUploadOriginalFileName).getFileName().toString();

        try {

            Path uploadPath = Paths.get(TEMP_DIR, uploadId);

            validateChunks(uploadId, totalChunks);

            Path finalFilePath = uploadPath.resolve(originalFileName);

            try (OutputStream os = Files.newOutputStream(finalFilePath, StandardOpenOption.CREATE_NEW)) {

                for (int i = 0; i < totalChunks; i++) {

                    Path chunkPath = uploadPath.resolve("chunk_" + i + ".part");

                    Files.copy(chunkPath, os);
                }

                deleteChunks(uploadPath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteChunks(Path chunkFilesPath) {
        try (Stream<Path> files = Files.list(chunkFilesPath)) {
            files.filter(p -> p.toString().endsWith(".part"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.out.println("Erro ao deletar chunk");
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateChunks(String uploadId, int totalChunks) {

        checkFileExist(uploadId);

        try {
            Path uploadPath = Paths.get(TEMP_DIR, uploadId);
            try (Stream<Path> files = Files.list(uploadPath)) {
                long existingChunks = files
                        .filter(p -> p.getFileName().toString().endsWith(".part"))
                        .count();
                if (existingChunks != totalChunks) {
                    throw new NoSuchChunkException();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void checkFileExist(String uploadId) {
        Path uploadPath = Paths.get(TEMP_DIR, uploadId);
        if (!Files.exists(uploadPath)) {
            throw new UploadNotExistException(uploadId);
        }
    }
}
