package com.shijiawei.secretblog.article.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ClassName: InternalApiKeyFilter
 * Description:
 *
 * @Create 2026/1/15 下午2:27
 */
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${custom.internal.apikey}")
    private String expectedApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // 只對/internal/路徑進行驗證
        if (requestURI.contains("/internal/")) {
            String apiKey = request.getHeader("X-Internal-Api-Key");

            //如果APIKey為空或不匹配，拒絕請求
            if (apiKey == null || !apiKey.equals(expectedApiKey)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"內部 API 金鑰無效或缺失\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
