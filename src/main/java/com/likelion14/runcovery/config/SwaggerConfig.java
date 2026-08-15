package com.likelion14.runcovery.config;

import com.likelion14.runcovery.common.CurrentUserId;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUserId.class);
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("런커버리 API")
                        .description("Runcovery API 명세서")
                        .version("v1.0.0"))
                //.addSecurityItem(new SecurityRequirement().addList("X-Public-Id"))
                .components(new Components()
                        .addSecuritySchemes("X-Public-Id", new SecurityScheme()
                                .name("X-Public-Id")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)))
                .tags(List.of(
                        new Tag().name("1. User").description("회원가입/내정보/홈/마이페이지 API"),
                        new Tag().name("2. Goal").description("미래목표/주간목표 설정 및 조회 API"),
                        new Tag().name("3. Condition").description("컨디션 체크/오늘의 미션 API"),
                        new Tag().name("4. Body Issue").description("통증부위 등록/조회 API"),
                        new Tag().name("5. Activity").description("러닝 활동기록 동기화/조회 API"),
                        new Tag().name("6. Running Report").description("맞춤형 웰니스 러닝 리포트 API"),
                        new Tag().name("7. Wellness Skin").description("웰니스 피부 스캔/기록/비교 API"),
                        new Tag().name("8. Wellness Prescription").description("맞춤형 웰니스 처방전 API")
                ));
    }
}
