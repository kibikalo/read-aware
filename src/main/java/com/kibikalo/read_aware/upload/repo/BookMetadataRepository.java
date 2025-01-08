package com.kibikalo.read_aware.upload.repo;

import com.kibikalo.read_aware.upload.model.BookMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookMetadataRepository extends JpaRepository<BookMetadata, Long> {

}