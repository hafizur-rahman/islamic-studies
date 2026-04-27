package com.jdreamer.repository;

import com.jdreamer.model.Verb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerbRepository extends JpaRepository<Verb, Integer> {
}
