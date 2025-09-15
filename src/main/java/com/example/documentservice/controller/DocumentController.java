package com.example.documentservice.controller;

import com.example.documentservice.entity.Document;
import com.example.documentservice.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class DocumentController {
    @Autowired
    private DocumentService documentService;

    @GetMapping("/document/{id}")
    public String getDocumentbyId(@PathVariable("id") String id) {
        return this.documentService.findById(id).toString();
    }

    @GetMapping("/document")
    public String getDocuments(@PathVariable("id") String id) {
        return this.documentService.findAll().toString();
    }

    @PostMapping("/document")
    public String uploadDocument(@RequestBody Document document){/*@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "title", required = false) String title,
                                  @RequestParam(value = "created", required = false) String created,
                                  @RequestParam(value = "correspondent", required = false) String correspondent,
                                  @RequestParam(value = "document_type", required = false) String documentType,
                                  @RequestParam(value = "storage_path", required = false) String storagePath,
                                  @RequestParam(value = "tags", required = false) List<String> tags,
                                  @RequestParam(value = "archive_serial_number", required = false) String archiveSerialNumber,
                                  @RequestParam(value = "custom_fields", required = false) List<String> customFields)*/
        return this.documentService.upload(document).toString();
    }

    /*@PutMapping("/document/{id}")
    public String update(@PathVariable("id") String id){
        return this.documentService.update(id).toString();
    }*/
}
