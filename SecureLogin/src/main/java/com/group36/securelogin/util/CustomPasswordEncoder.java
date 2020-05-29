package com.group36.securelogin.util;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

public class CustomPasswordEncoder {

    private final static int cpuCost = (int) Math.pow(2, 14);    // Factor to increase CPU costs
    private final static int memoryCost = 8;                     // Increases memory usage
    private final static int parallelization = 1;                // Currently not supported by Spring Security
    private final static int keyLength = 32;                     // Key length in bytes
    private final static int saltLength = 64;                    // Salt length in bytes

    private CustomPasswordEncoder() {
    }

    public static PasswordEncoder passwordEncoder() {
        return new SCryptPasswordEncoder(
                cpuCost,
                memoryCost,
                parallelization,
                keyLength,
                saltLength);
    }
}
