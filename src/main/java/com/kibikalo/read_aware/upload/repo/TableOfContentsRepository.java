package com.kibikalo.read_aware.upload.repo;

import com.kibikalo.read_aware.upload.model.TableOfContents;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableOfContentsRepository extends JpaRepository<TableOfContents, Long> {}
