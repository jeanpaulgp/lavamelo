package com.lavamelo.web;

import com.lavamelo.auth.RolInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RolInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/logout");
    }

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info().title("Lava-Melo API").version("1.0")
                .description("Inicie sesión con POST /api/auth/login; la cookie de sesión se usa en el resto de endpoints."));
    }
}
