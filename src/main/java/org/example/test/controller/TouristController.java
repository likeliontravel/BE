package org.example.test.controller;

import lombok.RequiredArgsConstructor;
import org.example.test.dto.TouristAttractionDto;
import org.example.test.service.TouristService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/tourist-attractions")
@RequiredArgsConstructor
public class TouristController {

    private final TouristService touristService;

    @GetMapping
    public Mono<ResponseEntity<List<TouristAttractionDto>>> getTouristAttractions(@RequestParam String city) {
        return touristService.getTouristAttractions(city)
                .map(dtoList -> ResponseEntity.ok().body(dtoList))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}