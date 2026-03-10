package com.company.observability.starter.integration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
class TestController {

    @GetMapping("/demo/ok")
    Map<String, Object> ok() {
        return Map.of("ok", true);
    }

    @GetMapping("/demo/boom")
    Map<String, Object> boom() {
        throw new RuntimeException("Sensitive database error");
    }
}
