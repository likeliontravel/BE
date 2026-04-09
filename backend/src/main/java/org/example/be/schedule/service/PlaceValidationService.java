package org.example.be.schedule.service;

import lombok.RequiredArgsConstructor;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.place.entity.PlaceType;
import org.example.be.place.accommodation.repository.AccommodationRepository;
import org.example.be.place.restaurant.repository.RestaurantRepository;
import org.example.be.place.touristSpot.repository.TouristSpotRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceValidationService {

    private final TouristSpotRepository touristSpotRepository;
    private final RestaurantRepository restaurantRepository;
    private final AccommodationRepository accommodationRepository;

    public void validateContentIdByPlaceType(PlaceType placeType, String contentId) {
        boolean exists = switch (placeType) {
            case TouristSpot -> touristSpotRepository.findByContentId(contentId).isPresent();
            case Restaurant -> restaurantRepository.findByContentId(contentId).isPresent();
            case Accommodation -> accommodationRepository.findByContentId(contentId).isPresent();
        };

        if (!exists) {
            throw new BusinessException(ErrorCode.PLACE_NOT_FOUND, "placeType: " + placeType + ", contentId: " + contentId);
        }
    }
}
