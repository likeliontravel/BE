package org.example.be.resolver;

import com.nimbusds.oauth2.sdk.util.URIUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DecodedPathVariableResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(DecodedPathVariable.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFacotry) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        @SuppressWarnings("unchecked")
        Map<String, String> uriTemplateVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        String parameterName = parameter.getParameterName();
        DecodedPathVariable annotation = parameter.getParameterAnnotation(DecodedPathVariable.class);
        if (annotation != null && !annotation.value().isEmpty()) {
            parameterName = annotation.value();
        }

        String rawValue = uriTemplateVars.get(parameterName);
        if (rawValue == null) {
            throw new IllegalArgumentException("PathVariable " + parameterName + " not found in URI variables");
        }

        return UriUtils.decode(rawValue, StandardCharsets.UTF_8);
    }
}
