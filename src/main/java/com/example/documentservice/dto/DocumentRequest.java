package com.example.documentservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRequest {
    private String documentId;
    private String content;
}
