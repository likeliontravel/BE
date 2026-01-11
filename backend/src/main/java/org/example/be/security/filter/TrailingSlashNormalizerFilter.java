package org.example.be.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
    Next.js 프로젝트 특성 상 라우팅 도중 엔드포인트 뒤에 /가 붙어버리는 문제를 해결하기 위해 생성한 필터
    컨트롤러 빈으로 리퀘스트 매핑으로 지정된 URI 뒤에 /가 붙은 경우 제거해준다.
 */
@Component
public class TrailingSlashNormalizerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 예외 : 루트(/), 웹소켓(/ws)
        if ("/".equals(uri) || uri.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 끝에 / 있는 경우만 forward
        if (uri.length() > 1 && uri.endsWith("/")) {
            String normalizedUri = uri.replaceAll("/+$", "");
            String qs = request.getQueryString();
            if (qs != null && !qs.isEmpty()) {
                normalizedUri += "?" + qs;
                if (logger.isDebugEnabled()) {
                    logger.debug("TrailingSlashNormalizerFilter: forward {" + uri + "} -> {" + normalizedUri + "}");
                }
            }

            request.getRequestDispatcher(normalizedUri).forward(request, response);
            return; // forward 했으니 종료
        }

        // 이미 정상 URI면 그냥 통과
        filterChain.doFilter(request, response);
    }
}