package com.likelion14.runcovery.common;

import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String HEADER_NAME = "X-User-Id";

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String header = request.getHeader(HEADER_NAME);

        if (header == null || header.isBlank()) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, HEADER_NAME + " 헤더가 필요합니다");
        }

        UUID publicId;
        try {
            publicId = UUID.fromString(header);
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, HEADER_NAME + " 형식이 올바르지 않습니다");
        }

        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다"));

        return user.getId();
    }
}
