package org.example.be.tour_api.batch;

import java.util.Map;

import org.example.be.place.accommodation.entity.Accommodation;
import org.example.be.place.accommodation.repository.AccommodationRepository;
import org.example.be.place.region.TourRegionRepository;
import org.example.be.place.restaurant.entity.Restaurant;
import org.example.be.place.restaurant.repository.RestaurantRepository;
import org.example.be.place.theme.PlaceCategoryRepository;
import org.example.be.place.touristSpot.entity.TouristSpot;
import org.example.be.place.touristSpot.repository.TouristSpotRepository;
import org.example.be.tour_api.batch.processor.AccommodationItemProcessor;
import org.example.be.tour_api.batch.processor.PlaceProcessorHelper;
import org.example.be.tour_api.batch.processor.RestaurantItemProcessor;
import org.example.be.tour_api.batch.processor.TouristSpotItemProcessor;
import org.example.be.tour_api.batch.reader.TourApiItemReader;
import org.example.be.tour_api.batch.writer.AccommodationItemWriter;
import org.example.be.tour_api.batch.writer.RestaurantItemWriter;
import org.example.be.tour_api.batch.writer.TouristSpotItemWriter;
import org.example.be.tour_api.dto.FetchResult;
import org.example.be.tour_api.service.RefreshRegionService;
import org.example.be.tour_api.util.TourApiClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {

	private final RefreshRegionService refreshRegionService;
	private final TourApiClient tourApiClient;
	private final TourRegionRepository tourRegionRepository;
	private final PlaceCategoryRepository placeCategoryRepository;
	private final TouristSpotRepository touristSpotRepository;
	private final RestaurantRepository restaurantRepository;
	private final AccommodationRepository accommodationRepository;

	@Value("${service-key}")
	private String serviceKey;

	/**
	 * 전체 Tour 데이터 갱신 Job
	 *
	 * Step 순서 :
	 * 1. refreshRegionStep - TourRegion 갱신
	 * 2. touristSpotFetchStep - TouristSpot 저장/업데이트 (chunk)
	 * 3. restaurantFetchStep - Restaurant 저장/업데이트 (chunk)
	 * 4. accommodationFetchStep - Accommodation 저장/업데이트 (chunk)
	 */
	@Bean
	public Job tourDataRefreshJob(
		JobRepository jobRepository,
		Step refreshRegionStep,
		Step touristSpotFetchStep,
		Step restaurantFetchStep,
		Step accommodationFetchStep
	) {
		return new JobBuilder("tourDataRefreshJob", jobRepository)
			.start(refreshRegionStep)
			.next(touristSpotFetchStep)
			.next(restaurantFetchStep)
			.next(accommodationFetchStep)
			.build();
	}

	/**
	 * Step 1. TourRegion 갱신 (Tasklet 방식)
	 *
	 * 기존 RefreshRegionService.refreshRegion() 그대로 호출
	 * Tasklet : "한 번에 쭉 실행"하는 단순 Step방식
	 * -> 대량 데이터의 chunk 처리가 필요 없는 작업에 적합
	 */
	@Bean
	public Step refreshRegionStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("refreshRegionStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				log.info("[Batch Step 1] TourRegion 갱신 시작");
				FetchResult result = refreshRegionService.refreshRegions();

				StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
				stepExecution.getExecutionContext().putInt("savedCount", result.saved());
				stepExecution.getExecutionContext().putInt("updatedCount", result.updated());
				stepExecution.getExecutionContext().putInt("skippedCount", result.skipped());
				stepExecution.getExecutionContext().putInt("failedCount", result.failed());

				log.info("[Batch Step 1] TourRegion 갱신 완료");
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}

	/**
	 * Step 2: TouristSpot 저장/업데이트 (Chunk 방식)
	 *
	 * - Reader: TourApi에서 데이터 수집
	 * - Processor: TouristSpot 엔티티로 변환 + 변경 감지
	 * - Writer: DB 저장
	 * - Chunk size: 100
	 *
	 * 내부에서 실패 발생 시 BATCH_STEP_EXECUTION테이블의 SKIP_COUNT 에 카운트 기록됨.
	 *
	 * - 실행 결과는 BATCH_STEP_EXECUTION 테이블에 아래와 같이 정보가 남음
	 * 	- READ_COUNT : 읽은 총 개수
	 * 	- WRITE_COUNT : 저장 성공한 개수
	 * 	- FILTER_COUNT : Processor가 null 반환한 개수 (SKIPPED)
	 * 	- SKIP_COUNT : 예외로 건너뛴 개수 (FAILED)
	 */
	@Bean
	public Step touristSpotFetchStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
		PlaceProcessorHelper processorHelper) {
		return new StepBuilder("touristSpotFetchStep", jobRepository)
			.<Map<String, Object>, TouristSpot>chunk(100, transactionManager)
			.reader(touristSpotItemReader())
			.processor(touristSpotItemProcessor(processorHelper))
			.writer(touristSpotItemWriter())
			.faultTolerant()    // 오류가 발생해도 전체가 멈추지 않도록 내결함성 모드 설정.
			.skip(Exception.class)    // 이 예외 발생 시 건너뛰기. 단, 실패 발생 시 내부에서 failed count + 1
			.skipLimit(Integer.MAX_VALUE) // 최대 몇 개까지 건너뛸지 (Integer 최대범위를 넘어갈 경우 스킵)
			.build();
	}

	/**
	 * TouristSpot Reader
	 * contentTypeId=12 (관광지)
	 */
	@Bean
	public TourApiItemReader touristSpotItemReader() {
		return new TourApiItemReader(
			tourApiClient,
			tourRegionRepository,
			serviceKey,
			12,
			9999
		);
	}

	/**
	 * TouristSpot Processor
	 */
	@Bean
	public TouristSpotItemProcessor touristSpotItemProcessor(PlaceProcessorHelper processorHelper) {
		return new TouristSpotItemProcessor(
			touristSpotRepository,
			processorHelper
		);
	}

	/**
	 * TouristSpot Writer
	 */
	@Bean
	public TouristSpotItemWriter touristSpotItemWriter() {
		return new TouristSpotItemWriter(touristSpotRepository);
	}

	/**
	 * Step 3: Restaurant 저장 / 업데이트 (Chunk 방식)
	 *
	 * - Reader: TourAPI에서 contentTypeId=39 데이터 수집
	 * - Processor: Restaurant 엔티티로 변환 + 변경 감지
	 * - Writer: DB 저장
	 * - Chunk size: 100
	 */
	@Bean
	public Step restaurantFetchStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
		PlaceProcessorHelper processorHelper) {
		return new StepBuilder("restaurantFetchStep", jobRepository)
			.<Map<String, Object>, Restaurant>chunk(100, transactionManager)
			.reader(restaurantItemReader())
			.processor(restaurantItemProcessor(processorHelper))
			.writer(restaurantItemWriter())
			.faultTolerant()
			.skip(Exception.class)
			.skipLimit(Integer.MAX_VALUE)
			.build();
	}

	/**
	 * Restaurant Reader
	 * contentTypeId = 39 (식당)
	 */
	@Bean
	public TourApiItemReader restaurantItemReader() {
		return new TourApiItemReader(
			tourApiClient,
			tourRegionRepository,
			serviceKey,
			39,
			9999
		);
	}

	/**
	 * Restaurant Processor
	 */
	@Bean
	public RestaurantItemProcessor restaurantItemProcessor(PlaceProcessorHelper processorHelper) {
		return new RestaurantItemProcessor(
			restaurantRepository,
			processorHelper
		);
	}

	/**
	 * Restaurant Writer
	 */
	@Bean
	public RestaurantItemWriter restaurantItemWriter() {
		return new RestaurantItemWriter(restaurantRepository);
	}

	/**
	 * Step 4: Accommodation 저장 / 업데이트 (Chunk방식)
	 *
	 * - Reader: TourAPI에서 contentTypeId=32 데이터 수집
	 * - Processor: Accommodation 엔티티로 변환 + 변경 감지
	 * - Writer: DB 저장
	 * - Chunk Size: 100
	 */
	@Bean
	public Step accommodationFetchStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
		PlaceProcessorHelper processorHelper) {
		return new StepBuilder("accommodationFetchStep", jobRepository)
			.<Map<String, Object>, Accommodation>chunk(100, transactionManager)
			.reader(accommodationItemReader())
			.processor(accommodationItemProcessor(processorHelper))
			.writer(accommodationItemWriter())
			.faultTolerant()
			.skip(Exception.class)
			.skipLimit(Integer.MAX_VALUE)
			.build();
	}

	/**
	 * Accommodation Reader
	 * contentTypeId = 32 (숙소)
	 */
	@Bean
	public TourApiItemReader accommodationItemReader() {
		return new TourApiItemReader(
			tourApiClient,
			tourRegionRepository,
			serviceKey,
			32,
			9999
		);
	}

	/**
	 * Accommodation Processor
	 */
	@Bean
	public AccommodationItemProcessor accommodationItemProcessor(PlaceProcessorHelper processorHelper) {
		return new AccommodationItemProcessor(
			accommodationRepository,
			processorHelper
		);
	}

	/**
	 * Accommodation Writer
	 */
	@Bean
	public AccommodationItemWriter accommodationItemWriter() {
		return new AccommodationItemWriter(accommodationRepository);
	}
}
