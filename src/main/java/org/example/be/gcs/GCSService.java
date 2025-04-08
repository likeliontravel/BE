package org.example.be.gcs;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GCSService {

    private final Storage storage;

    @Value("${gcs.bucket.profile}")
    private String profileBucketName;

    /**
     * 프로필 사진 GCS 업로드 메서드
     * param : 저장할 이미지파일, userIdentifier
     * @return : 저장 성공 후 반환받은 public URL
     */
    public String uploadProfileImage(MultipartFile file, String userIdentifier) throws IOException {
        String fileName = "profile_" + userIdentifier + "_" + UUID.randomUUID();
        BlobId blobId = BlobId.of(profileBucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
        storage.create(blobInfo, file.getBytes());

        return String.format("https://storage.googleapis.com/%s/%s", profileBucketName, fileName);
    }

    /**
     * 프로필 사진 버킷에서 삭제 메서드
     * param : image public URL
     */
    public void deleteProfileImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains(profileBucketName)) {
            return;
        }
        String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        BlobId blobId = BlobId.of(profileBucketName, fileName);
        storage.delete(blobId);
    }

}
