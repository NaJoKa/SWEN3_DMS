package com.example.documentservice.controller;

import com.example.documentservice.dto.DocumentSearchResultDto;
import com.example.documentservice.service.DocumentSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = DocumentController.class)
public class DocumentControllerSearchTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentSearchService documentSearchService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSearchWithLanguageEng() throws Exception {
        String query = "language = \"eng\"";
        Pageable pageable = PageRequest.of(0, 10);

        DocumentSearchResultDto dto = new DocumentSearchResultDto(1L, "file.pdf", "summary", 1L, "objectKey", 1.0, "snippet");
        Mockito.when(documentSearchService.search(Mockito.any(), Mockito.eq(query), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto), pageable, 1));

        mockMvc.perform(get("/documents/search")
                        .param("query", query)
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
