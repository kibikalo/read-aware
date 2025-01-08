package com.kibikalo.read_aware.upload.repo;

import com.kibikalo.read_aware.upload.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
}

