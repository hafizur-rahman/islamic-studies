package com.jdreamer.repository;

import com.jdreamer.model.Verb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerbRepository extends JpaRepository<Verb, Integer> {
    List<Verb> findNounsByBookIdAndPageId(int bookId, int pageId);

    Optional<Verb> findByWord(String word);
}
