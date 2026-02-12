package org.example.be.place.theme;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, String> {
	// cat3 (PK)로 테마 얻을 때 이용
	Optional<PlaceCategory> findByCat3(String cat3);

	// theme로 해당 cat3 정보가 존재하는지 여부만 true or false 반환
	boolean existsPlaceCategoryByTheme(String theme);

	/** Fallback용 : theme로 PlaceCategory 조회
	 *  first 사용 이유 : theme에 해당하는 튜플이 여러 개(ex-기타)여도 첫 번째 하나만 반환( 예외 안터짐 )
	 */
	Optional<PlaceCategory> findFirstByTheme(String theme);

}
