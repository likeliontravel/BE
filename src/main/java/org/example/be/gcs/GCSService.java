package org.example.be.gcs;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.example.be.exception.custom.GCSDeletionFailedException;
import org.example.be.exception.custom.GCSUploadFailedException;
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
        try {
            validateImageFile(file);
            String fileName = "profile_" + userIdentifier + "_" + UUID.randomUUID();
            BlobId blobId = BlobId.of(profileBucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
            storage.create(blobInfo, file.getBytes());

            return String.format("https://storage.googleapis.com/%s/%s", profileBucketName, fileName);
        } catch (IOException e) {
            throw new GCSUploadFailedException("[GCS] 프로필 이미지 업로드 실패. ", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage()); // 그대로 전파
        }

    }

    /**
     * 프로필 사진 버킷에서 삭제 메서드
     * param : image public URL
     */
    public void deleteProfileImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains(profileBucketName)) {
            return;
        }

        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            BlobId blobId = BlobId.of(profileBucketName, fileName);
            boolean deleted = storage.delete(blobId);

            if (!deleted) {
                System.out.println("[GCS 프로필 이미지 삭제 이상] - 삭제하려는 파일이 존재하지 않아 삭제되지 않았습니다. fileName: " + fileName);
            }
        } catch (Exception e) {
            throw new GCSDeletionFailedException("GCS 프로필 이미지 삭제 중 오류가 발생했습니다.", e);
        }

    }

    /**
     * 채팅 이미지 업로드 메서드
     *
     */
    public String uploadChatImage(MultipartFile file, String senderIdentifier, String groupName) throws IOException {
        try {
            validateImageFile(file);
            String fileName = "chat_" + groupName + "_" + senderIdentifier + "_" + UUID.randomUUID();
            BlobId blobId = BlobId.of(chatImageBucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
            storage.create(blobInfo, file.getBytes());

            return String.format("https://storage.googleapis.com/%s/%s", chatImageBucketName, fileName);
        } catch (IOException e) {
            throw new GCSUploadFailedException("[GCS] 채팅 이미지 업로드 실패. ", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    // 입력받은 파일이 이미지 파일인지 확인 ( 이미지 파일이 아닐 경우 예외 발생 )
    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }


}
