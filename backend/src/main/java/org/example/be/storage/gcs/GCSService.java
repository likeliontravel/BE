package org.example.be.storage.gcs;

import java.io.IOException;
import java.util.UUID;

import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

import lombok.RequiredArgsConstructor;

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
	 * param : 저장할 이미지파일, 업로드하는 회원의 memberId
	 * @return : 저장 성공 후 반환받은 public URL
	 */
	public String uploadProfileImage(MultipartFile file, Long memberId) {
		try {
			validateImageFile(file);
			String fileName = "profile_" + memberId + "_" + UUID.randomUUID();
			BlobId blobId = BlobId.of(profileBucketName, fileName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
			storage.create(blobInfo, file.getBytes());

			return String.format("https://storage.googleapis.com/%s/%s", profileBucketName, fileName);
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED, "프로필 이미지 업로드 실패. \nmessage: " + e.getMessage());
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
			throw new BusinessException(ErrorCode.GCS_DELETE_FAILED,
				"image url: " + imageUrl + ", message: " + e.getMessage());
		}

	}

	/**
	 * 채팅 이미지 업로드 메서드
	 *
	 */
	public String uploadChatImage(MultipartFile file, String senderId, String groupName) {
		try {
			validateImageFile(file);
			String fileName = "chat_" + groupName + "_" + senderId + "_" + UUID.randomUUID();
			BlobId blobId = BlobId.of(chatImageBucketName, fileName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
			storage.create(blobInfo, file.getBytes());

			return String.format("https://storage.googleapis.com/%s/%s", chatImageBucketName, fileName);
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED, "채팅 이미지 업로드 실패. \n message: " + e.getMessage());
		}
	}

	// 입력받은 파일이 이미지 파일인지 확인 ( 이미지 파일이 아닐 경우 예외 발생 )
	private void validateImageFile(MultipartFile file) {
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE_TYPE, "입력된 파일 contentType: " + contentType);
		}
	}

	// legacy/board/GcsUploader에 기존에 설계되었던 GcsUploader 담아둡니다. Board GCS 사용 방식 결정되면 그 때 이 클래스에 클라이언트 코드 추가하세요. - 2026.04.14

}
