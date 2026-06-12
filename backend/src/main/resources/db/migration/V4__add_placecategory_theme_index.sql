-- place_category.theme 조회 성능 인덱스 (PlaceCategoryRepository.findFirstByTheme 대상)
-- 운영 DB(toleave)에는 2026-06-12 수동 CREATE INDEX로 이미 생성됨(마이그레이션 미기록 상태였음).
-- 신규/로컬 DB에는 없으므로, 환경 간 SSOT를 맞추기 위해 멱등 처리로 작성한다.
--   - 운영 DB: 인덱스가 이미 존재 → SKIP, Flyway는 V4를 '적용됨'으로 기록
--   - 신규/로컬 DB: 인덱스 없음 → CREATE
SET @exists := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'place_category'
      AND index_name = 'idx_placecategory_theme'
  );
  SET @ddl := IF(@exists = 0,
    'CREATE INDEX idx_placecategory_theme ON place_category (theme)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;