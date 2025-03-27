package org.example.test.controller;

import lombok.RequiredArgsConstructor;
import org.example.test.entity.TouristSpot;
import org.example.test.service.TouristSpotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tourism")
@RequiredArgsConstructor
public class TourismController {

    private final TouristSpotService touristSpotService;

    @GetMapping("/fetch/{areaCode}")
    public ResponseEntity<List<TouristSpot>> fetchAndSaveTouristSpots(@PathVariable String areaCode) {
        List<TouristSpot> savedSpots = touristSpotService.fetchAndSaveTouristSpots(areaCode);
        return ResponseEntity.ok(savedSpots);
    }

    @GetMapping("/{areaCode}")
    public ResponseEntity<List<TouristSpot>> getTouristSpots(@PathVariable String areaCode) {
        List<TouristSpot> spots = touristSpotService.getTouristSpotsByAreaCode(areaCode);
        return ResponseEntity.ok(spots);
    }
}