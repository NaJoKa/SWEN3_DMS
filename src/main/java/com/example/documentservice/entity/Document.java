package com.example.documentservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @Column(length = 36)
    private int id;

    // --- getters & setters ---
    /*public int getId() { return id; }
    public void setId(String id) { this.id = id; }*/
    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    @Column(length = 5000)
    private String summary;

    @Getter
    @Column(name = "ocr_text", length = 100000)
    private String ocrText;

    @Setter
    @Getter
    private LocalDateTime created;

    private String correspondent;
    private String documentType;
    private String storagePath;
    private String archiveSerialNumber;

    /*@ElementCollection
    @CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "tag")
    private List<String> tags;

    @ElementCollection
    @CollectionTable(name = "document_custom_fields", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "custom_field")
    private List<String> customFields;*/

    public Document() { }

    /*public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<String> getCustomFields() { return customFields; }
    public void setCustomFields(List<String> customFields) { this.customFields = customFields; }*/
}
