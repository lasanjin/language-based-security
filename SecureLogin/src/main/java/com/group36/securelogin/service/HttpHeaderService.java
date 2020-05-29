package com.group36.securelogin.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class HttpHeaderService {

    private final HttpServletRequest request;

    public HttpHeaderService(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * The X-Forwarded-For (XFF) header is a de-facto standard header for identifying the originating
     * IP address of a client connecting to a web server through an HTTP proxy or a load balancer.
     *
     * @return IP address of the client or last proxy that sent the request
     */
    public String getClientIP() {
        String xffHeader = request.getHeader("X-Forwarded-For");
        if (xffHeader == null) {
            return request.getRemoteAddr();
        }
        // X-Forwarded-For: [client, proxy1, proxy2]
        return xffHeader.split(",")[0];
    }
}
