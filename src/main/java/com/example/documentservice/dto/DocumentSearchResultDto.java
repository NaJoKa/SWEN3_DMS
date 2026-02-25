package com.example.documentservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchResultDto {
    private Long id;
    private String name;
    private String summary;
    private Long ownerId;
    private String objectKey;
    // search result specific fields
    private Double score;
    private String snippet;
}
