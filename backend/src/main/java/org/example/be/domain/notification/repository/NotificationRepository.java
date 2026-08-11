package org.example.be.domain.notification.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.example.be.domain.notification.entity.Notification;
import org.example.be.domain.notification.type.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

	//읽지 않은 알림 갯수 조회
	long countByReceiverIdAndReadFalse(Long receiverId);

	// 알림 모두 읽음 처리
	@Modifying
	@Query("UPDATE Notification n SET n.read = true, n.readAt = :now WHERE n.receiver.id = :receiverId AND n.read = false")
	int markAllAsRead(@Param("receiverId") Long receiverId, @Param("now") java.time.LocalDateTime now);

	@Modifying
	@Query("DELETE FROM Notification n WHERE n.receiver.id = :receiverId")
	void deleteByReceiverId(@Param("receiverId") Long receiverId);

	//그룹, 일정, 게시글 삭제 시 관련 알림 삭제
	@Modifying
	@Query("DELETE FROM Notification n WHERE n.type IN :types AND n.targetId = :targetId")
	void deleteByTypeInAndTargetId(@Param("types") List<NotificationType> types, @Param("targetId") Long targetId);

	// 30일 뒤 삭제용
	@Modifying
	@Query("DELETE FROM Notification n WHERE n.createdTime < :threshold")
	int deleteByCreatedTimeBefore(@Param("threshold") LocalDateTime threshold);

}
