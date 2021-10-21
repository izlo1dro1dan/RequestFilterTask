package ru.test.application.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class TestControllerTest {

    @Autowired
    private MockMvc mvc;

    private int threads = 255;

    @Test
    void test1() {
        Integer [] arr = new Integer[threads];
        for (int i = 0; i < threads; i++) {
            arr[i]=i;
        }
        Arrays.stream(arr).parallel().forEach(i -> {
                    try {
                        Integer status = mvc.perform(get("/test")
                                .with(request -> {
                                    request.setRemoteAddr("192.168.0." + i);
                                    return request;
                                })).andReturn().getResponse().getStatus();
                        assertEquals(Integer.valueOf(200),status);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }
}