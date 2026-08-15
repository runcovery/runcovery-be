package com.likelion14.runcovery.config;

import com.likelion14.runcovery.common.CurrentUserId;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class CurrentUserIdOperationCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        boolean needsUserId = java.util.Arrays.stream(handlerMethod.getMethodParameters())
                .anyMatch(p -> p.hasParameterAnnotation(CurrentUserId.class));

        if (needsUserId) {
            operation.addParametersItem(new Parameter()
                    .in("header")
                    .name("X-User-Id")
                    .required(true)
                    .description("유저 식별용 UUID")
                    .example("11111111-1111-1111-1111-111111111111")
                    .schema(new io.swagger.v3.oas.models.media.StringSchema()));
        }
        return operation;
    }
}
