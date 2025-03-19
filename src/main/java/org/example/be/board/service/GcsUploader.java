package org.example.be.board.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GcsUploader {

    private final Storage storage;

    @Value("${gcs.bucket.toleave}")
    private String bucketName;

    // 생성자에서 Storage 객체를 생성하면서 인증을 명시적으로 설정
    public GcsUploader() throws IOException {
        this.storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(new FileInputStream("/Users/yeboong99/Desktop/merge/src/main/resources/toleave-b9a7b3a17267.json"))).build().getService();
    }

    @Transactional
    public String uploadImage(MultipartFile file) throws IOException {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new IOException("파일이 비어 있습니다.");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String storeFileName = UUID.randomUUID() + "_" + originalFilename;

            BlobId blobId = BlobId.of(bucketName, storeFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
            // GCS에 파일 업로드
            storage.create(blobInfo, file.getBytes());

            // 업로드한 파일의 GCS URL 반환
            return String.format("https://storage.googleapis.com/%s/%s", bucketName, storeFileName);

        } catch (Exception e) {
            throw new IOException("GCS 업로드 실패: " + e.getMessage(), e);
        }
    }

    public void deleteImage(String storeFileName) throws IOException {
        try {
            // URL 인코딩된 파일명 추출
            String encodedFileName = storeFileName.substring(storeFileName.lastIndexOf("/") + 1); // 파일명만 추출
            System.out.println("인코딩된 파일 이름: " + encodedFileName);

            // BlobId로 객체 가져오기
            Blob blob = storage.get(BlobId.of(bucketName, encodedFileName));
            if (blob == null) {
                System.out.println("GCS에서 파일을 찾을 수 없습니다: " + encodedFileName);
            } else if (blob.exists()) {
                // 파일이 존재하면 삭제
                blob.delete();
                System.out.println("파일 삭제: " + encodedFileName);
            } else {
                System.out.println("파일이 존재하지 않음: " + encodedFileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("GCS 파일 삭제 실패: " + e.getMessage(), e);
        }
    }
}