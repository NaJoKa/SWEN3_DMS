package com.example.documentservice.controller;

import com.example.documentservice.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class DocumentController {
    @Autowired
    private DocumentService documentService;

    @GetMapping("/document/{id}")
    public String getDocumentbyId(@PathVariable("id") String id) {
        return this.documentService.findById(id).toString();
    }
    @PostMapping("/document")
    public String uploadDocument(@PathVariable MultipartFile file) {
        return this.documentService.upload();
    }



}
