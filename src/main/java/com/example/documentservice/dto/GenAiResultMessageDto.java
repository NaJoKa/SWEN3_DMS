package com.example.documentservice.dto;

public record GenAiResultMessageDto(String processedMessage, String summary, String objectKey, String elasticId, String fileName) { }
