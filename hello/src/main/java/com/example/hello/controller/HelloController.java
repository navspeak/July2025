package com.example.hello.controller;


import com.example.hello.jpa.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

@RestController
public class HelloController {
    @Autowired
    AuditService auditService;

    @GetMapping("/hello")
    public Long sayHello() {

        return auditService.createAuditWithMappings(LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                LongStream.range(1,600).boxed().toList() );
    }

    @GetMapping("/env")
    public Map<String, String> env() {
        return System.getenv();  // Returns all environment variables as a Map
    }
}