package org.example.be.place;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.be.place.entity.Place;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlaceService {

    @PersistenceContext
    private EntityManager entityManager;

    // 지역, 테마를 입력해 숙소 또는 관광지 또는 식당을 조회 (타입 지정형입니다. 저장코드 완성 후 머지하면서 추가로 조회로직 작성할 예정)
    public <T extends Place> List<T> getSpecifiedTypePlacesByRegionAndTheme(Class<T> placeType, String siGunGuCode, String theme) {
        String jpql = "SELECT p FROM " + placeType.getSimpleName() + " p WHERE 1=1";

        if (siGunGuCode != null && !siGunGuCode.isEmpty()) {
            jpql += " AND p.siGunGuCode = :siGunGuCode";
        }

        if (theme != null && !theme.isEmpty()) {
            jpql += " AND p.theme = :theme";
        }

        TypedQuery<T> query = entityManager.createQuery(jpql, placeType);

        if (siGunGuCode != null && !siGunGuCode.isEmpty()) {
            query.setParameter("siGunGuCode", siGunGuCode);
        }

        if (theme != null && !theme.isEmpty()) {
            query.setParameter("theme", theme);
        }

        return query.getResultList();
    }


}
