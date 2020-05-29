package com.group36.securelogin.service;

import com.group36.securelogin.model.User;
import com.group36.securelogin.util.CustomPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * Use UserDetailsService when passwords are provided in own database/data model,
 * else (when using a different authentication system) use the AuthenticationProvider
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private Map<String, User> users;
    private final int MAX_ATTEMPTS = 3;

    private final HttpServletRequest request;
    private final LoginAttemptService loginAttemptService;
    private final HttpHeaderService httpHeaderService;

    public CustomUserDetailsService(
            HttpServletRequest request,
            LoginAttemptService loginAttemptService,
            HttpHeaderService httpHeaderService) {

        this.users = new HashMap<>();
        this.request = request;
        this.loginAttemptService = loginAttemptService;
        this.httpHeaderService = httpHeaderService;

        User user = buildUser("admin", "nintendo", "ADMIN");
        this.users.put(user.getUsername(), user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        if (!users.containsKey(username)) {
            throw new UsernameNotFoundException("User not found: " + username); // Server-side exception
        }

        User user = users.get(username);

        /** Brute-force countermeasure */
        /*
        user.updateAttempts(false); // Increment # attempts
        if (loginAttemptService.isBlocked(httpHeaderService.getClientIP())
                && user.getAttempts() > MAX_ATTEMPTS) {
            throw new LockedException("Too many attempts, user blocked");
        }
         */
        /******************************/

        return mapUserDetails(user);
    }

    private UserDetails mapUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles())
                .build();
    }

    private User buildUser(String username, String password, String... roles) {
        String encodedPassword = CustomPasswordEncoder.passwordEncoder().encode(password);
        return new User(username, encodedPassword, roles);
    }

    public User getUser(String username) {
        return users.getOrDefault(username, null);
    }
}
