package org.example.be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BeApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeApplication.class, args);
	}

	//    @Bean   // 빈으로 등록된 모든 컨트롤러 엔드포인트 로그에서 보기
	//    public ApplicationRunner printMappings(RequestMappingHandlerMapping mapping) {
	//        return args -> mapping.getHandlerMethods()
	//                .forEach((info, method) -> System.out.println(info + " -> " + method));
	//    }
}
