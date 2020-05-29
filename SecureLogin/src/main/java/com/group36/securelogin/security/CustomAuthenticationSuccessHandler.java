package com.group36.securelogin.security;

import com.group36.securelogin.model.User;
import com.group36.securelogin.service.CustomUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final CustomUserDetailsService customUserDetailsService;

    public CustomAuthenticationSuccessHandler(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        handle(response, authentication);
        clearAuthenticationAttributes(request);
    }

    private void handle(HttpServletResponse response, Authentication authentication) throws IOException {
        String username = ((org.springframework.security.core.userdetails.User)
                authentication.getPrincipal()).getUsername();

        User user = customUserDetailsService.getUser(username);
        user.updateAttempts(true); // Reset # attempts

        response.sendRedirect("/");
    }

    /**
     * Removes temporary authentication-related data which may have been stored in the session
     * during the authentication process.
     *
     * @param request
     */
    private void clearAuthenticationAttributes(final HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}
