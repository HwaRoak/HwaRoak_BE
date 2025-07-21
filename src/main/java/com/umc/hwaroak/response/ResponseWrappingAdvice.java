package com.umc.hwaroak.response;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@Hidden
public class ResponseWrappingAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {

        String className = returnType.getDeclaringClass().getName();

        if (className.contains("springdoc") || className.contains("OpenApiResource")) {
            return false;
        }

        return !ResponseEntity.class.isAssignableFrom(returnType.getParameterType())
                && !String.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body instanceof ErrorResponse || body instanceof ApiResponse<?>) {
            return body;
        }

        return ApiResponse.success(SuccessCode.OK, body);
    }
}
