package com.example.documentservice.service.mapper;

import com.example.documentservice.dto.DocumentDto;
import com.example.documentservice.entity.Document;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    DocumentDto toDto(Document doc);
}
