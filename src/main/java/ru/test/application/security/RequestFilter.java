package ru.test.application.security;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Value;


import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RequestFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestFilter.class);

    @Value("${count}")
    private Integer maxRequestCount;

    @Value("${minute}")
    private Integer maxRequestTime = 1;


    private final LoadingCache<String, Integer> requestCountsPerIpAddress;

    public RequestFilter(){
        super();
        requestCountsPerIpAddress = CacheBuilder.newBuilder()
                .expireAfterWrite(maxRequestTime, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String clientIpAddress = getClientIP((HttpServletRequest) request);
        if(isMaximumRequestsPerSecondExceeded(clientIpAddress)){
            log.error("Too many request");
            httpServletResponse.setStatus(HttpStatus.BAD_GATEWAY.value());
            httpServletResponse.getWriter().write("Too many requests");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isMaximumRequestsPerSecondExceeded(String clientIpAddress){
        int requests = 1;
        try {
            requests = requestCountsPerIpAddress.get(clientIpAddress);
            if(requests > maxRequestCount){
                requestCountsPerIpAddress.put(clientIpAddress, requests);
                return true;
            }
        } catch (ExecutionException e) {
            log.error(e.getMessage());
        }
        requests++;
        requestCountsPerIpAddress.put(clientIpAddress, requests);
        return false;
    }

    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null){
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
