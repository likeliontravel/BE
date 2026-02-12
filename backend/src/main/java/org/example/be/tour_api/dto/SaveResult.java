package org.example.be.tour_api.dto;

/**
 * 개별 데이터 저장 작업의 결과를 나타내는 enum
 * - SAVED : 신규 저장
 * - UPDATED : 기존 데이터 변경 시 변경 감지 후 업데이트
 * - SKIPPED : 기존 데이터와 동일하여 무변경
 */
public enum SaveResult {
	SAVED,
	UPDATED,
	SKIPPED
}
