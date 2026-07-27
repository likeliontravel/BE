package org.example.be.domain.chat.type;

public enum SearchDirection {
	BEFORE, // 커서보다 과거 매치 ( 스크롤 업 )
	AFTER,  // 커서보다 최신 매치 ( 스크롤 다운 )
	BOTH    // 커서(특정 메시지)를 중심으로 전후 메시지를 한 번에 ( 검색 결과 클릭 시 그 위치로 점프 )
}
