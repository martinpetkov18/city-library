package com.citylibrary.citylibrary.repository;

import com.citylibrary.citylibrary.model.Reader;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReaderRepository extends JpaRepository<Reader, String> {
    Reader findByNameIgnoreCase(String name);
}