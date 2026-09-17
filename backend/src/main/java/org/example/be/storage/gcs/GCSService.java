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
import com.google.cloud.storage.StorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GCSService {

	private final Storage storage;

	@Value("${gcs.bucket.profile}")
	private String profileBucketName;

	@Value("${gcs.bucket.chat}")
	private String chatImageBucketName;

	@Value("${gcs.bucket.toleave}")
	private String boardImageBucketName;

	/**
	 * 프로필 사진 GCS 업로드 메서드
	 * param : 저장할 이미지파일, 업로드하는 회원의 memberId
	 * @return : 저장 성공 후 반환받은 public URL
	 */
	public String uploadProfileImage(MultipartFile file, Long memberId) {
		// 검증은 I/O가 아니므로 try 밖에 둔다.
		validateImageFile(file);

		try {
			String fileName = "profile_" + memberId + "_" + UUID.randomUUID();
			BlobId blobId = BlobId.of(profileBucketName, fileName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
			storage.create(blobInfo, file.getBytes());

			return String.format("https://storage.googleapis.com/%s/%s", profileBucketName, fileName);
		} catch (IOException | StorageException e) {
			// StorageException(인증, 버킷, 권한, 네트워크 등 GCS SDK 장애)은 RuntimeException 이라 여기에 명시해야 잡힌다.
			// validateImageFile 이 try 밖에 있어야 400(잘못된 파일)이 GCS_UPLOAD_FAILED(500)로 승격되지 않는다.
			throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED, "프로필 이미지 업로드 실패. memberId: " + memberId, e);
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
				// 호출처는 프로필 교체 또는 삭제(MemberService)다. 삭제 대상이 없다는 것은 DB에 저장된 URL 이
				// 버킷의 실제 객체와 어긋났다는 데이터 불일치 신호이므로, 멱등으로 넘기지 않고 WARN 으로 남긴다.
				log.warn("[GCS 프로필 이미지 삭제 이상] - 삭제하려는 파일이 존재하지 않아 삭제되지 않았습니다. fileName: {}", fileName);
			}
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.GCS_DELETE_FAILED, "imageUrl: " + imageUrl, e);
		}

	}

	/**
	 * 채팅 이미지 업로드 메서드
	 *
	 */
	public String uploadChatImage(MultipartFile file, String senderId, String groupName) {
		// 검증은 I/O가 아니므로 try 밖에 둔다.
		validateImageFile(file);

		try {
			String fileName = "chat_" + groupName + "_" + senderId + "_" + UUID.randomUUID();
			BlobId blobId = BlobId.of(chatImageBucketName, fileName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
			storage.create(blobInfo, file.getBytes());

			return String.format("https://storage.googleapis.com/%s/%s", chatImageBucketName, fileName);
		} catch (IOException | StorageException e) {
			throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED,
				"채팅 이미지 업로드 실패. groupName: " + groupName + ", senderId: " + senderId, e);
		}
	}

	// 입력받은 파일이 이미지 파일인지 확인 ( 이미지 파일이 아닐 경우 예외 발생 )
	// 게시글 이미지 업로드에서 전량으로 사진을 검증하기 위해서 public으로 변경
	public void validateImageFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE_TYPE, "빈 파일은 업로드할 수 없습니다.");
		}
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE_TYPE, "입력된 파일 contentType: " + contentType);
		}
	}

	/**
	 * 게시글 이미지 GCS 업로드 메서드
	 * param : 저장할 이미지파일, 업로드하는 회원의 memberId
	 * @return : 저장 성공 후 반환받은 public URL
	 */
	public String uploadBoardImage(MultipartFile file, Long memberId) {
		// 검증은 I/O가 아니므로 try 밖에 둔다.
		validateImageFile(file);

		try {
			String fileName = "board_" + memberId + "_" + UUID.randomUUID();
			BlobId blobId = BlobId.of(boardImageBucketName, fileName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
			storage.create(blobInfo, file.getBytes());

			return String.format("https://storage.googleapis.com/%s/%s", boardImageBucketName, fileName);
		} catch (IOException | StorageException e) {
			throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED,
				"게시글 이미지 업로드 실패. memberId: " + memberId, e);
		}
	}

	/**
	 * 게시글 이미지 버킷에서 삭제 메서드
	 * param : image public URL
	 */
	public void deleteBoardImage(String imageUrl) {
		if (imageUrl == null || !imageUrl.contains(boardImageBucketName)) {
			return;
		}

		try {
			String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
			BlobId blobId = BlobId.of(boardImageBucketName, fileName);
			boolean deleted = storage.delete(blobId);

			if (!deleted) {
				// WARN 인 이유: deleteProfileImage() 의 같은 분기 주석 참조
				log.warn("[GCS 게시글 이미지 삭제 이상] - 삭제하려는 파일이 존재하지 않아 삭제되지 않았습니다. fileName: {}", fileName);
			}
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.GCS_DELETE_FAILED, "imageUrl: " + imageUrl, e);
		}

	}

}
