package com.example.documentservice.controller;

import com.example.documentservice.entity.Document;
import com.example.documentservice.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerUnitTest {

    @Mock
    DocumentRepository documentRepository;

    @InjectMocks
    DocumentController controller;

    private Document doc(Integer id, String title, String summary) {
        Document d = new Document();
        d.setId(id == null ? 0 : id);
        d.setTitle(title);
        d.setSummary(summary);
        d.setCreated(LocalDateTime.now());
        d.setCorrespondent("Alice");
        d.setDocumentType("txt");
        d.setStoragePath("/tmp/test.txt");
        d.setArchiveSerialNumber("ARCH-1");
        return d;
    }

    @Test
    void getAllDocuments_returnsOkWithList() {
        when(documentRepository.findAll()).thenReturn(List.of(doc(1,"A","s1"), doc(2,"B","s2")));

        ResponseEntity<List<Document>> res = controller.getAllDocuments();

        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(res.getBody()).hasSize(2);
        verify(documentRepository).findAll();
    }

    @Test
    void getDocumentById_found_returnsDocument() {
        when(documentRepository.findById(1)).thenReturn(Optional.of(doc(1,"A","s")));

        Document out = controller.getDocumentById(1);

        assertThat(out).isNotNull();
        assertThat(out.getTitle()).isEqualTo("A");
        verify(documentRepository).findById(1);
    }

    @Test
    void getDocumentById_notFound_returnsNull() {
        when(documentRepository.findById(99)).thenReturn(Optional.empty());

        Document out = controller.getDocumentById(99);

        assertThat(out).isNull();
        verify(documentRepository).findById(99);
    }

    @Test
    void uploadDocument_callsSave_returnsOk() {
        Document incoming = doc(null,"Neu","neu summary");
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> {
            Document d = inv.getArgument(0);
            d.setId(123);
            return d;
        });

        ResponseEntity<Document> res = controller.uploadDocument(incoming);

        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getId()).isEqualTo(123);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Neu");
    }

    @Test
    void updateDocumentByID_found_updatesAndSaves() {
        Document existing = doc(7,"Alt","old");
        when(documentRepository.findById(7)).thenReturn(Optional.of(existing));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        Document patch = doc(null,"IGNORED","new-summary");

        ResponseEntity<Document> res = controller.updateDocumentById(7, patch);

        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getSummary()).isEqualTo("new-summary");
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void updateDocumentByID_notFound_returns404() {
        when(documentRepository.findById(77)).thenReturn(Optional.empty());

        ResponseEntity<Document> res = controller.updateDocumentById(77, doc(null,"X","Y"));

        assertThat(res.getStatusCode().value()).isEqualTo(404);
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void deleteDocumentByID_existing_returnsNoContentAndDeletes() {
        when(documentRepository.existsById(5)).thenReturn(true);

        ResponseEntity<Void> res = controller.deleteDocumentById(5);

        assertThat(res.getStatusCode().value()).isEqualTo(204);
        verify(documentRepository).deleteById(5);
    }

    @Test
    void deleteDocumentByID_missing_returns404_noDelete() {
        when(documentRepository.existsById(6)).thenReturn(false);

        ResponseEntity<Void> res = controller.deleteDocumentById(6);

        assertThat(res.getStatusCode().value()).isEqualTo(404);
        verify(documentRepository, never()).deleteById(anyInt());
    }
}
