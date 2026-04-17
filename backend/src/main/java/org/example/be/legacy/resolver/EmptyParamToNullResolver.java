//package org.example.be.resolver;
//
//import org.springframework.core.MethodParameter;
//import org.springframework.web.bind.support.WebDataBinderFactory;
//import org.springframework.web.context.request.NativeWebRequest;
//import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
//import org.springframework.web.method.support.HandlerMethodArgumentResolver;
//import org.springframework.web.method.support.ModelAndViewContainer;
//
//import java.util.List;
//
//// 파라미터 값으로 빈 문자열이 들어오는 경우 ""를 값으로 넣지 않고 파라미터가 없는 것으로 처리하기 위한 리졸버.
//// 뭔가 이상함 실제로는 제대로 적용 안되고있음 다시 확인해야함..

//// 적용 안되는 이유: Spring MVC의 ArgumentResolver 체인 우선순위 구조 때문
//// Spring MVC 내부 기본 리졸버 목록 (먼저 등록)
///     └── RequestParamMethodArgumentResolver  ← @RequestParam 처리
///     └── PathVariableMethodArgumentResolver
///     └── ...
///
///   addArgumentResolvers()로 추가한 커스텀 리졸버 (뒤에 추가)
///     └── EmptyParamToNullResolver  ← 절대 호출 안됨
///
///   EmptyParamToNullResolver.supportsParameter()는 내부적으로 defaultResolver.supportsParameter()를 그대로 위임. 즉 @RequestParam이 붙은
///   파라미터라면 true를 반환하는데, 이미 Spring 기본 리졸버가 먼저 @RequestParam을 처리해버리므로 커스텀 리졸버까지 체인이 내려오지 않음.
///
///   이 문제를 근본적으로 해결하려면 WebMvcConfigurer.addArgumentResolvers()가 아닌, RequestMappingHandlerAdapter에 직접 접근해서 리졸버 목록의
///   맨 앞에 삽입해야 함.
///
///   ---
///   3. 왜 전체가 주석 처리되었는가
///
///   코드와 주석을 종합하면 다음 순서로 상황이 전개됐습니다:
///
///   ① 빈 문자열 파라미터 문제 발생
///      └─ 프론트가 ?regions=&themes= 전송 시 잘못된 필터 동작
///
///   ② EmptyParamToNullResolver 작성 + WebConfig에 등록 시도
///
///   ③ 동작하지 않음 확인
///      └─ WebConfig.java:25 주석: "이거 똑바로 적용 안되고있음"
///
///   ④ 원인 파악 실패 (시간 부족 등) → 일단 주석 처리
///
///   ⑤ 서비스 레이어에서 직접 null 변환으로 우회 처리
///
///   우회 처리의 증거 — TouristSpotFilterService.java:30, RestaurantFilterService.java:34, AccommodationFilterService.java:31 에서 동일한 패턴이
///    3개 서비스 모두에 중복 존재:
///
///   // 파라미터가 빈 리스트라면 null 로 변환 → JPQL에서 무시되도록
///   if (regions != null && regions.isEmpty()) {
///       regions = null;
///   }
///   if (themes != null && themes.isEmpty()) {
///       themes = null;
///   }
///   if (keyword != null && keyword.isBlank()) {
///       keyword = null;
///   }
///
///   즉, 리졸버 레벨에서 해결하려다 실패하자 서비스 레이어 3곳에 동일한 방어 코드를 복붙한 상태
///   이 파일은 현재 동작하지 않는 채로 남겨진 미완성 시도의 흔적이며, 삭제하거나 RequestMappingHandlerAdapter를 이용해 올바르게 재구현하는 것이 깔끔한 해결책
//public class EmptyParamToNullResolver implements HandlerMethodArgumentResolver {
//
//    private final RequestParamMethodArgumentResolver defaultResolver;
//
//    public EmptyParamToNullResolver(RequestParamMethodArgumentResolver defaultResolver) {
//        this.defaultResolver = defaultResolver;
//    }
//
//    @Override
//    public boolean supportsParameter(MethodParameter parameter) {
//        return defaultResolver.supportsParameter(parameter);
//    }
//
//    @Override
//    public Object resolveArgument(MethodParameter parameter,
//                                  ModelAndViewContainer mavContainer,
//                                  NativeWebRequest webRequest,
//                                  WebDataBinderFactory binderFactory) throws Exception {
//
//        Object resolved = defaultResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
//
//        // 단일 String 파라미터: "" → null
//        if (resolved instanceof String str && str.trim().isEmpty()) {
//            return null;
//        }
//
//        if (resolved instanceof List<?> list) {
//            List<String> cleaned = list.stream()
//                    .map(v -> v == null ? null : v.toString().trim())
//                    .filter(v -> v != null && !v.isEmpty())
//                    .toList();
//            return cleaned.isEmpty() ? null : cleaned;
//        }
//
//        return resolved;
//    }
//}