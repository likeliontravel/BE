package org.example.be.security.config;

import org.example.be.resolver.DecodedPathVariableResolver;
import org.example.be.security.filter.TrailingSlashNormalizerFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;

// WebMvcConfigurer를 이용한 설정은 전부 몰아두기.
// 현재 resolver 등록, MVC DispatcherServlet 레벨에서 동작할 설정 등록.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 커스텀 ArgumentResolver - PathVariable UTF-8 디코더 등록
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new DecodedPathVariableResolver());
    }

    // Spring MVC 디스패쳐 서블렛 레벨에서 동작할 CORS 잡아주기 (!= SecurityConfig에서 등록한 필터체인 레벨 CORS 와 다름)
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        corsRegistry.addMapping("/**")
                .exposedHeaders("Set-Cookie")
                .allowedOrigins("https://localhost:3000")
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);

        corsRegistry.addMapping("/ws/**")
                .allowedOrigins("https://localhost:3000")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // nginx 뒤에 있을 때 헤더 처리용 필터
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> bean = new FilterRegistrationBean<>(new ForwardedHeaderFilter());
        bean.setOrder(0); // 가장 먼저 실행되도록 설정
        return bean;
    }

    // uri 맨 뒤 /를 없애주는 필터를 보안필터보다 무조건 앞으로 오도록 배치
    // filter.TrailingSlashNormalizerFilter 클래스에서 빈으로 설정함. 지금 이 부분 삭제 예정
//    @Bean
//    public FilterRegistrationBean<TrailingSlashNormalizerFilter> trailingSlashNormalizerFilter(TrailingSlashNormalizerFilter filter) {
//        FilterRegistrationBean<TrailingSlashNormalizerFilter> reg = new FilterRegistrationBean<>(filter);
//        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
//        reg.addUrlPatterns("/*");
//        return reg;
//    }


}
