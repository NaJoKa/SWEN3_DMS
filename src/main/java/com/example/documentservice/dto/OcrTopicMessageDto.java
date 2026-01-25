package com.example.documentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class OcrTopicMessageDto {
    @JsonProperty("objectKey")
    private String objectKey;
    @JsonProperty("bucket")
    private String bucket;

    @Override
    public String toString() {
        return "OcrTopicMessageDto{" +
               "objectKey='" + objectKey + '\'' +
               ", bucket='" + bucket + '\'' +
               '}';
    }
}
