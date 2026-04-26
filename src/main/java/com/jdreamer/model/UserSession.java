package com.jdreamer.model;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table
@Data
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "BOOK_ID")
    int bookId;

    @Column(name = "PAGE_ID")
    int pageId;

    @Column(name = "ACCESSED_AT")
    long accessedAt;
}
