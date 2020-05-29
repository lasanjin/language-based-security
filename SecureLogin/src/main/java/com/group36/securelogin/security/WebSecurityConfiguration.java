package com.group36.securelogin.security;

import com.group36.securelogin.security.filter.CustomFilter;
import com.group36.securelogin.service.CustomUserDetailsService;
import com.group36.securelogin.util.CustomPasswordEncoder;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomUserDetailsService customUserDetailsService;

    public WebSecurityConfiguration(
            CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
            CustomUserDetailsService customUserDetailsService) {

        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(CustomPasswordEncoder.passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterAfter(new CustomFilter(), BasicAuthenticationFilter.class) // Same-site
                .authorizeRequests()
                .antMatchers("/index.html").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .and()
                    .requiresChannel()
                    .regexMatchers("/login.html").requiresSecure() // Force HTTPS
                .and()
                    .formLogin()
                        .loginProcessingUrl("/login") // URL to validate the credentials
                        .loginPage("/login").permitAll() // Custom login page
                        .successHandler(customAuthenticationSuccessHandler)
                        .usernameParameter("username-form")
                        .passwordParameter("password-form")
                .and()
                    .logout()
                        //.logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // csrf().disable()
                        .logoutSuccessUrl("/login")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true) // Default
                        .deleteCookies("JSESSIONID")
                .and()
                    .rememberMe()// Ads another cookie after session expiration
                        .tokenValiditySeconds(86400) // 1 day
                        .key("unique-key") // Token: hash(username, password, expiration time, key)
                        .rememberMeParameter("remember-me-check");
                //.and()
                    //.csrf().disable();         // Disable Cross-Site Request Forgery (CSRF) attack protection
                    //.headers().disable()      // Disable default security headers
                    //.cacheControl()           // Cache Control
                    //.frameOptions()           // X-Frame-Options
                    //.contentTypeOptions()     // Content Type Options
                    //.hsts()                   // HTTP Strict Transport Security (HSTS)
                    //.frameOptions()           // X-Frame-Options
                    //.xssProtection();         // X-XSS-Protection
    }

}
