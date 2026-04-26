package com.jdreamer.model;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "FILE_PATH")
    String filePath;

    @Column(name = "TITLE")
    String title;

    @Column(name = "LAST_ACCESSED")
    long lastAccessed;
}
