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

    @Value("${gcs.bucket.chat}")
    private String chatImageBucketName;

    /**
     * 프로필 사진 GCS 업로드 메서드
     * param : 저장할 이미지파일, userIdentifier
     * @return : 저장 성공 후 반환받은 public URL
     */
    public String uploadProfileImage(MultipartFile file, String userIdentifier) throws IOException {
        validateImageFile(file);
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

    /**
     * 채팅 이미지 업로드 메서드
     *
     */
    public String uploadChatImage(MultipartFile file, String senderIdentifier, String groupName) throws IOException {
        validateImageFile(file);
        String fileName = "chat_" + groupName + "_" + senderIdentifier + "_" + UUID.randomUUID();
        BlobId blobId = BlobId.of(chatImageBucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
        storage.create(blobInfo, file.getBytes());

        return String.format("https://storage.googleapis.com/%s/%s", chatImageBucketName, fileName);
    }

    // 입력받은 파일이 이미지 파일인지 확인 ( 이미지 파일이 아닐 경우 예외 발생 )
    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }


}
