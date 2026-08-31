package com.project.roomloop.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/listing")
@RequiredArgsConstructor
@Slf4j
public class ListingController {

    private final PasswordEncoder passwordEncoder;

    @GetMapping("/{name}")
    public String welcomeUser(@PathVariable String name){
        log.info("not wqroking");
        return "Welcome to this "+ name +" and this the there encoder version = " + passwordEncoder.encode(name);
    }

}
