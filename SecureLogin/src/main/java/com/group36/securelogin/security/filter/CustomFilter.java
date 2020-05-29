package com.group36.securelogin.security.filter;

import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Simple base implementation of Filter which treats its config parameters as bean properties.
 * This generic filter base class has no dependency on the Spring ApplicationContext concept.
 */
public class CustomFilter extends GenericFilterBean {

    /**
     * The doFilter method of the Filter is called by the container each time a request/response
     * pair is passed through the chain due to a client request for a resource at the end of the chain.
     *
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("set-cookie", "locale=en; HttpOnly; SameSite=Strict");
        chain.doFilter(request, response);
    }
}