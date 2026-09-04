package com.project.roomloop.dto;

import lombok.Data;

@Data
public class RegisterNewListingRequest {
    private String preferences;
    private int openSpots;
}
