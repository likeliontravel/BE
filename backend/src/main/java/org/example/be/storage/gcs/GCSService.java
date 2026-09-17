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
			// TODO: StorageException(GCS SDK 장애 - 인증, 버킷, 권한, 네트워크)은 RuntimeException이라 이 catch에 걸리지 않는다.
			// -> 진짜 GCS 장애는 GCS_UPLOAD_FAILED 가 아니라 catch-all의 INTERNAL_SERVER_ERROR 로 나간다.
			// -> 왜 지금 안 하는가: 응답 code가 바뀌는 프론트엔드 계약 변경이라 이후 작업 Phase에서 다룬다.
			// -> 올바른 해결: catch (IOException | StorageException e) 로 넓혀 GCS_UPLOAD_FAILED로 통일한다.
			//    (validateImageFile을 try 밖으로 뺐으므로 넓혀도 400이 500으로 승격되지 않는다)
			// -> 언제: Phase 2 의 3번째 작업 외부 API 경계 예외 정리하기로 결정함
		} catch (IOException e) {
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
				// TODO: System.out을 로거로 교체해야 한다.
				// - 왜 지금 안 하는가: Task 1-4는 예외 재포장을 다루고, 이 줄은 예외가 아니라 로깅 결함이다.
				// - 올바른 해결: log.warn으로 교체 + '삭제 대상 없음' 이 정상(멱등)인지 이상인지 확정
				// - 언제: Task 2-6 로깅 정비
				System.out.println("[GCS 프로필 이미지 삭제 이상] - 삭제하려는 파일이 존재하지 않아 삭제되지 않았습니다. fileName: " + fileName);
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
			// TODO: StorageException 은 RuntimeException이라 이 catch에 걸리지 않는다. (상세: 위 uploadProfileImage 의 같은 TODO).
			// - 올바른 해결: catch (IOException | StorageException e) 로 통일
			// - 언제: Phase 2 - 3 Task
		} catch (IOException e) {
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
			// TODO: StorageException 은 RuntimeException이라 이 catch에 걸리지 않는다. (상세: 위 uploadProfileImage 의 같은 TODO).
			// - 올바른 해결: catch (IOException | StorageException e) 로 통일
			// - 언제: Phase 2 - 3 Task
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED, "게시글 이미지 업로드 실패. memberId: " + memberId, e);
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
				// TODO: System.out 을 로거로 교체해야 한다 ( 상세: deleteProfileImage() 와 같은 TODO.)
				System.out.println("[GCS 게시글 이미지 삭제 이상] - 삭제하려는 파일이 존재하지 않아 삭제되지 않았습니다. fileName: " + fileName);
			}
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.GCS_DELETE_FAILED, "imageUrl: " + imageUrl, e);
		}

	}

}
