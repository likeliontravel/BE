package org.example.be.place.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.place.dto.TouristSpotDTO;
import org.example.be.place.service.TouristSpotService;
import org.example.be.place.util.AreaCodeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tour")
@RequiredArgsConstructor
public class TourismController {

    private final TouristSpotService touristSpotService;
    private final AreaCodeResolver areaCodeResolver;
    private static final Logger logger = LoggerFactory.getLogger(TourismController.class);

    @GetMapping("/fetch/{areaCode}")
    public ResponseEntity<List<TouristSpotDTO>> fetch(@PathVariable String areaCode, @RequestParam(defaultValue = "1") int pageNo) throws Exception{
        int code = Integer.parseInt(areaCode);
        String state = areaCodeResolver.getState(code);
        if (state == null) {
            throw new IllegalArgumentException("유효하지 않은 지역 코드입니다. areaCode: " + areaCode);
        }

        List<TouristSpotDTO> result = touristSpotService.getTouristSpots(
                code, state, 12, 1000, pageNo
        );

        return ResponseEntity.ok(result);
    }
}