package com.project.roomloop.controller;

import com.project.roomloop.dto.BillGenerateRequestDto;
import com.project.roomloop.dto.BillGenerateResponseDto;
import com.project.roomloop.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms/{roomId}/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping("/generate")
    public ResponseEntity<BillGenerateResponseDto> generateBill(@PathVariable Long roomId,
                                                                @RequestBody BillGenerateRequestDto request,
                                                                @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.generateBill(roomId, userId, request));
    }

    @GetMapping
    public List<BillGenerateResponseDto> getBillHistory(@PathVariable Long roomId,
                                                        @AuthenticationPrincipal Long userId) {
        return billService.getBillHistory(roomId, userId);
    }
}