package com.kibikalo.read_aware.upload;

import com.kibikalo.read_aware.upload.model.BookData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookDataRepository extends JpaRepository<BookData, Long> {

}