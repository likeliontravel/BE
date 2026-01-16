package org.example.be.tour_api.service;

import org.example.be.place.region.TourRegionRepository;
import org.example.be.tour_api.util.TourApiClient;
import org.example.be.tour_api.util.TourApiParser;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshRegionService {

	private final TourApiClient tourApiClient;
	private final TourApiParser tourApiParser;
	private TourRegionRepository tourRegionRepository;

	public void getRefreshedRegion() {

	}

}
