package com.example.documentservice.repository;

import com.example.documentservice.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface DocumentRepository extends JpaRepository<Document, Integer> {
    Page<Document> findAllByOwnerId(Long ownerId, Pageable pageable);

    java.util.Optional<Document> findByObjectKey(String objectKey);

    java.util.List<Document> findAllByOwnerIdAndOpensearchIdIsNotNull(Long ownerId);
}
