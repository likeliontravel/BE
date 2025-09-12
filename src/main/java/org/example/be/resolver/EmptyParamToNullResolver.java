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