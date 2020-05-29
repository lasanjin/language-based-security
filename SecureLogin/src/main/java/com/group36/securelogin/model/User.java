package com.group36.securelogin.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class User {

    private String username;
    private String password;
    private String[] roles;
    private int attempts;
    private Date lastAttempt;

    public User(String username, String password, String... roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.attempts = 0;
    }

    public int updateAttempts(boolean reset) {
        this.attempts = reset ? 0 : this.attempts + 1;
        this.lastAttempt = reset ? null : new Date(System.currentTimeMillis());

        return this.attempts;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String[] getRoles() {
        return roles;
    }

    public int getAttempts() {
        return this.attempts;
    }

    public String getLastAttempt() {
        if (lastAttempt != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            return formatter.format(this.lastAttempt);
        }
        return "No attempts";
    }
}
