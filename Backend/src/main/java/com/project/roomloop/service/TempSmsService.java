package com.project.roomloop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TempSmsService implements SmsService {

    @Override
    public void send(String mobileNumber, String message) {

        log.info("MOCK SMS to {} : {}", mobileNumber, message);

    }
}
