package com.example.documentservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "documents")
public class Document implements IDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(length = 36)
    private int id;
    private String title;
    @Column(length = 5000)
    private String summary;
    @Column(name = "ocr_text", length = 100000)
    private String ocrText;
    private LocalDateTime created;
    private String correspondent;
    private String documentType;
    private String storagePath;
    private String archiveSerialNumber;

    public Document(String name) {
        this.title = name;
    }

    /*@ElementCollection
    @CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "tag")
    private List<String> tags;

    @ElementCollection
    @CollectionTable(name = "document_custom_fields", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "custom_field")
    private List<String> customFields;*/

    /*public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<String> getCustomFields() { return customFields; }
    public void setCustomFields(List<String> customFields) { this.customFields = customFields; }*/
}
