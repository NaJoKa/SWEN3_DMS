package com.example.documentservice.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
}
