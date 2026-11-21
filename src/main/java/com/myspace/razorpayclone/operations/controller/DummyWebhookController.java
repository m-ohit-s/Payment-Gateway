package com.myspace.razorpayclone.operations.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class DummyWebhookController {

    @PostMapping("/success")
    ResponseEntity<Void> handleWebhookSuccess(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.noContent().build();
    }
}
