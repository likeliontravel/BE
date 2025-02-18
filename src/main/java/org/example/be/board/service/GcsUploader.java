package org.example.be.board.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
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

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    public String uploadImage(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String storeFileName  = UUID.randomUUID() + "_" + originalFilename;

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, storeFileName ).build();
        Blob blob = storage.create(blobInfo, file.getInputStream());

        return String.format("https://storage.googleapis.com/%s/%s", bucketName, storeFileName );
    }
    public void deleteImage(String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        if (blob != null) {
            blob.delete();
        }
    }
}
