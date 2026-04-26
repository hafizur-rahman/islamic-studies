package com.jdreamer.model;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table
@Data
public class Verb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "BOOK_ID")
    int bookId;

    @Column(name = "PAGE_ID")
    int pageId;

    @Column(name = "WORD")
    String word;

    @Column(name = "BAB")
    String bab;

    @Column(name = "MASDAR")
    String masdar;

    @Column(name = "PAST")
    String past;

    @Column(name = "FUTURE")
    String future;

    @Column(name = "COMMAND")
    String command;

    @Column(name = "MEANING")
    String meaning;

    @Column(name = "EXAMPLE")
    String example;
}
