package at.fhtw.genAIworker.dto;


public record GenAiResultMessage(String processedMessage, String summary, String objectKey, String elasticId, String fileName) { }
