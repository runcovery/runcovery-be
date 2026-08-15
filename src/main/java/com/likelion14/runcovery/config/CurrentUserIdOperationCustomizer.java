package com.likelion14.runcovery.config;



import com.likelion14.runcovery.common.CurrentUserId;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;

@Component
public class CurrentUserIdOperationCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        boolean needsUserId = Arrays.stream(handlerMethod.getMethodParameters())
                .anyMatch(p -> p.hasParameterAnnotation(CurrentUserId.class));

        if (needsUserId) {
            operation.addSecurityItem(new SecurityRequirement().addList("X-Public-Id"));
        }
        return operation;
    }
}
