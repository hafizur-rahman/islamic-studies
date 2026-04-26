package com.jdreamer.model;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table
@Data
public class Noun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "BOOK_ID")
    int bookId;

    @Column(name = "PAGE_ID")
    int pageId;

    @Column(name = "WORD")
    String word;

    @Column(name = "SINGULAR")
    String singular;

    @Column(name = "DUAL")
    String dual;

    @Column(name = "PLURAL")
    String plural;

    @Column(name = "MEANING")
    String meaning;

    @Column(name = "EXAMPLE")
    String example;
}
