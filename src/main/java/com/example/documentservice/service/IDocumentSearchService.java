package com.example.documentservice.service;

import com.example.documentservice.dto.DocumentSearchResultDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IDocumentSearchService {
    Page<DocumentSearchResultDto> search(Long userId, String query, Pageable pageable);
}
