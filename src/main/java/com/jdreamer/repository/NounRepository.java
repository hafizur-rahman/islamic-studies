package com.jdreamer.repository;

import com.jdreamer.model.Noun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NounRepository extends JpaRepository<Noun, Integer> {
    List<Noun> findNounsByBookIdAndPageId(int bookId, int pageId);
}
