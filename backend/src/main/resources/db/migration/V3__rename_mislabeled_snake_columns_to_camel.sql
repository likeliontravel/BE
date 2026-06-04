-- =============================================================================
-- V3: V1__init.sql이 snake_case로 잘못 만든 컬럼들을 엔티티(SSOT, camelCase)에 맞게 정정.
--
-- 배경:
--   - 이 프로젝트의 Hibernate 명명 전략은 무변환(PhysicalNamingStrategyStandardImpl)이라,
--     @Column(name=...)이 없는 camelCase 필드는 DB 컬럼도 camelCase여야 한다.
--   - 그러나 V1__init.sql은 다수 컬럼을 snake로 만들어, 빈 DB 신규 배포 시 validate가 깨진다.
--
-- 안전성(핵심):
--   - 운영 DB는 과거 ddl-auto가 만든 camelCase 스키마라 snake 컬럼이 "이미 없다".
--     (baseline=V1이라 V1은 재실행되지 않지만, V2/V3는 운영 DB에서도 실행된다.)
--   - 따라서 각 RENAME을 "해당 snake 컬럼이 존재할 때만" 수행하도록 조건부로 만든다.
--       · 빈 DB : V1이 snake 컬럼 생성 → 조건 참 → 실제 RENAME 수행
--       · 운영 DB: snake 컬럼 없음        → 조건 거짓 → no-op (안전)
-- =============================================================================

-- 0. 재실행 안전을 위해 동일 프로시저가 남아있으면 제거
DROP PROCEDURE IF EXISTS rename_column_if_exists;

-- 1. 조건부 RENAME 헬퍼 프로시저: old 컬럼이 현재 스키마에 존재할 때만 new 로 변경
CREATE PROCEDURE rename_column_if_exists(
    IN p_table VARCHAR(64),
    IN p_old   VARCHAR(64),
    IN p_new   VARCHAR(64)
)
BEGIN
      IF EXISTS (
          SELECT 1
          FROM information_schema.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME   = p_table
            AND COLUMN_NAME  = p_old
      ) THEN
          SET @ddl = CONCAT(
              'ALTER TABLE `', p_table, '` RENAME COLUMN `', p_old, '` TO `', p_new, '`'
          );
          PREPARE stmt FROM @ddl;
          EXECUTE stmt;
          DEALLOCATE PREPARE stmt;
      END IF;
END;

  -- 2. snake → camel 정정 (총 41개 컬럼)

  -- member
CALL rename_column_if_exists('member', 'password_changed_at', 'passwordChangedAt');
CALL rename_column_if_exists('member', 'policy_agreed',       'policyAgreed');
CALL rename_column_if_exists('member', 'oauth_provider',      'oauthProvider');

-- board
CALL rename_column_if_exists('board', 'board_hits',           'boardHits');
CALL rename_column_if_exists('board', 'thumbnail_public_url', 'thumbnailPublicUrl');

-- comment
CALL rename_column_if_exists('comment', 'comment_content', 'commentContent');

-- user_groups (groupName 의 인라인 UNIQUE 는 RENAME 시 자동 반영)
CALL rename_column_if_exists('user_groups', 'group_name', 'groupName');

-- group_announcement
CALL rename_column_if_exists('group_announcement', 'writer_name', 'writerName');
CALL rename_column_if_exists('group_announcement', 'time_stamp',  'timeStamp');

-- group_invitation
--   주의: created_at/expires_at 는 Base 감사컬럼이 아니라 GroupInvitation 자체 camel 필드다.
--         (다른 테이블의 created_at/updated_at 은 진짜 Base 컬럼이므로 건드리지 않는다.)
CALL rename_column_if_exists('group_invitation', 'invitation_code', 'invitationCode');
CALL rename_column_if_exists('group_invitation', 'created_at',      'createdAt');
CALL rename_column_if_exists('group_invitation', 'expires_at',      'expiresAt');

-- schedule
CALL rename_column_if_exists('schedule', 'start_schedule', 'startSchedule');
CALL rename_column_if_exists('schedule', 'end_schedule',   'endSchedule');

-- schedule_place
--   주의: 여기 content_id 는 명시 @Column 이 없어 camel(contentId)로 정정한다.
--         반면 place 3종(아래)의 content_id 는 @Column(name="content_id")라 그대로 둔다.
CALL rename_column_if_exists('schedule_place', 'content_id',   'contentId');
CALL rename_column_if_exists('schedule_place', 'place_type',   'placeType');
CALL rename_column_if_exists('schedule_place', 'visit_start',  'visitStart');
CALL rename_column_if_exists('schedule_place', 'visited_end',  'visitedEnd');
CALL rename_column_if_exists('schedule_place', 'day_order',    'dayOrder');
CALL rename_column_if_exists('schedule_place', 'order_in_day', 'orderInDay');

-- accommodation (Place 상속 공통 컬럼)
CALL rename_column_if_exists('accommodation', 'image_url',           'imageUrl');
CALL rename_column_if_exists('accommodation', 'thumbnail_image_url', 'thumbnailImageUrl');
CALL rename_column_if_exists('accommodation', 'map_x',               'mapX');
CALL rename_column_if_exists('accommodation', 'map_y',               'mapY');
CALL rename_column_if_exists('accommodation', 'm_level',             'mLevel');
CALL rename_column_if_exists('accommodation', 'created_time',        'createdTime');
CALL rename_column_if_exists('accommodation', 'modified_time',       'modifiedTime');

-- restaurant (Place 상속 공통 컬럼)
CALL rename_column_if_exists('restaurant', 'image_url',           'imageUrl');
CALL rename_column_if_exists('restaurant', 'thumbnail_image_url', 'thumbnailImageUrl');
CALL rename_column_if_exists('restaurant', 'map_x',               'mapX');
CALL rename_column_if_exists('restaurant', 'map_y',               'mapY');
CALL rename_column_if_exists('restaurant', 'm_level',             'mLevel');
CALL rename_column_if_exists('restaurant', 'created_time',        'createdTime');
CALL rename_column_if_exists('restaurant', 'modified_time',       'modifiedTime');

-- tourist_spot (Place 상속 공통 컬럼)
CALL rename_column_if_exists('tourist_spot', 'image_url',           'imageUrl');
CALL rename_column_if_exists('tourist_spot', 'thumbnail_image_url', 'thumbnailImageUrl');
CALL rename_column_if_exists('tourist_spot', 'map_x',               'mapX');
CALL rename_column_if_exists('tourist_spot', 'map_y',               'mapY');
CALL rename_column_if_exists('tourist_spot', 'm_level',             'mLevel');
CALL rename_column_if_exists('tourist_spot', 'created_time',        'createdTime');
CALL rename_column_if_exists('tourist_spot', 'modified_time',       'modifiedTime');

-- 3. 헬퍼 프로시저 정리
DROP PROCEDURE rename_column_if_exists;