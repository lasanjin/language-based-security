package com.group36.securelogin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * If you want to keep Spring Boot MVC features and you want to add additional MVC configuration
 * (interceptors, formatters, view controllers, and other features), you can add your own
 * @Configuration class of type WebMvcConfigurer but without @EnableWebMvc.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    public WebMvcConfig() {
        super();
    }

    @Override
    public void configureDefaultServletHandling(final DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /**
     * Servlet listener that exposes the request to the current thread
     *
     * @return RequestContextListener
     */
    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

}