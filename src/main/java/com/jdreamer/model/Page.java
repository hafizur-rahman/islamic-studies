package com.jdreamer.model;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table
@Data
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "BOOK_ID")
    int bookId;

    @Column(name = "PAGE_NUMBER")
    int pageNumber;

    @Column(name = "CONTENT")
    String content;

    @Column(name = "TRANSLATION")
    String translation;
}
