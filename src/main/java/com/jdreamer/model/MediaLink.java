package com.jdreamer.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class MediaLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "BOOK_ID")
    int bookId;

    @Column(name = "PAGE_ID")
    int pageId;

    @Column(name = "URL")
    String url;

    @Column(name = "CHANNEL_NAME")
    String channelName;
}
