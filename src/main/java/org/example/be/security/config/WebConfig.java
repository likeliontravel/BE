package org.example.be.security.config;

import org.example.be.resolver.DecodedPathVariableResolver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
                .allowedOrigins("https://localhost:5500")
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);

        corsRegistry.addMapping("/ws/**")
                .allowedOrigins("https://localhost:5500")
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


}
