package com.rchat.randomChat.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rchat.randomChat.global.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final DispatcherServlet servlet;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        if (!checkEndpointExists(request)) {
            ApiResponse<?> apiResponse = ApiResponse.createError("No Content");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return;
        }
        ApiResponse<?> apiResponse = ApiResponse.createError(authException.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    private boolean checkEndpointExists(HttpServletRequest request) {
        List<HandlerMapping> mappingList = servlet.getHandlerMappings();
        if (mappingList == null) {
            return false;
        }
        for (HandlerMapping handlerMapping : servlet.getHandlerMappings()) {
            try {
                HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(request);
                return handlerExecutionChain != null;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}
