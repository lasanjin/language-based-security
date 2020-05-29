package com.group36.securelogin.security;

import com.group36.securelogin.service.HttpHeaderService;
import com.group36.securelogin.service.LoginAttemptService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final HttpHeaderService httpHeaderService;
    private final LoginAttemptService loginAttemptService;

    public AuthenticationSuccessEventListener(
            LoginAttemptService loginAttemptService, HttpHeaderService httpHeaderService) {

        this.loginAttemptService = loginAttemptService;
        this.httpHeaderService = httpHeaderService;
    }

    public void onApplicationEvent(AuthenticationSuccessEvent e) {
        loginAttemptService.loginSucceeded(httpHeaderService.getClientIP());
    }
}
