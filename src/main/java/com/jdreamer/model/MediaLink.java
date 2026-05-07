package com.jdreamer.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class MediaLink {
    @Id
    String id;

    @Column(name = "BOOK_ID")
    int bookId;

    @Column(name = "PAGE_ID")
    int pageId;
}
