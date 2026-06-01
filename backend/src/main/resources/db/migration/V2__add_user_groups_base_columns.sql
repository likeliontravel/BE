-- Group 엔티티가 Base 엔티티를 상속하면서 생기는 컬럼 업데이트
-- 기존 튜플은 created_at, updated_at 값이 Null로 채워지고, 이후 Auditing이 INSERT/UPDATE 시 자동 기록한다.

ALTER TABLE `user_groups`
    ADD COLUMN `created_at` DATETIME,
    ADD COLUMN `updated_at` DATETIME;