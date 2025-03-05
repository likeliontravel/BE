package org.example.be.board.service;

import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GcsUploader {

    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    @Value("${gcs.bucket.toleave}")
    private String bucketName;

    public String uploadImage(MultipartFile file) throws IOException {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new IOException("파일이 비어 있습니다.");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String storeFileName = UUID.randomUUID() + "_" + originalFilename;

            BlobId blobId = BlobId.of(bucketName, storeFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            // GCS에 파일 업로드
            storage.create(blobInfo, file.getBytes());

            // 업로드한 파일의 GCS URL 반환
            return String.format("https://storage.googleapis.com/%s/%s", bucketName, storeFileName);

        } catch (Exception e) {
            throw new IOException("GCS 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * GCS에서 이미지를 삭제
     *
     * @param fileName 삭제할 파일 이름
     */
    public void deleteImage(String fileName) {
        try {
            // BlobId로 객체 가져오기
            Blob blob = storage.get(BlobId.of(bucketName, fileName));
            if (blob != null && blob.exists()) {
                // 파일이 존재하면 삭제
                blob.delete();
            }
        } catch (Exception e) {
            throw new RuntimeException("GCS 파일 삭제 실패: " + e.getMessage(), e);
        }
    }
}
