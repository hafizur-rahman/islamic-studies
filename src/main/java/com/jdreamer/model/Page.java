package com.jdreamer.model;

import jakarta.persistence.*;

@Entity
@Table
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "BOOK_ID")
    int bookId;

    @Column(name = "PAGE_ID")
    int pageId;

    @Column(name = "CONTENT")
    String content;

    @Column(name = "TRANSLATION")
    String translation;
}
