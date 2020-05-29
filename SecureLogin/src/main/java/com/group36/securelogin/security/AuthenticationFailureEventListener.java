package com.group36.securelogin.security;

import com.group36.securelogin.service.HttpHeaderService;
import com.group36.securelogin.service.LoginAttemptService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureEventListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final HttpHeaderService httpHeaderService;
    private final LoginAttemptService loginAttemptService;

    public AuthenticationFailureEventListener(
            LoginAttemptService loginAttemptService, HttpHeaderService httpHeaderService) {

        this.loginAttemptService = loginAttemptService;
        this.httpHeaderService = httpHeaderService;
    }

    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
        loginAttemptService.loginFailed(httpHeaderService.getClientIP());
    }
}
