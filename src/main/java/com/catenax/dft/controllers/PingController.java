package com.catenax.dft.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class PingController {

    @GetMapping(value = "/ping")
    public ResponseEntity<String> getProcessReportById() {
        return ok().body(LocalDateTime.now().toString());
    }
}


