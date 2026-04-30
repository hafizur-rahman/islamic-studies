package com.jdreamer.model;

import lombok.Data;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "USER_SESSION")
@Data
@EqualsAndHashCode(of = {"bookId"})
public class UserSession {
    @Id
    @Column(name = "BOOK_ID")
    int bookId;

    @Column(name = "PAGE_ID")
    int pageId;

    @Column(name = "ACCESSED_AT")
    long accessedAt;

    @Column(name = "ZOOM_FACTOR")
    float zoomFactor = 1.0f;

    @Column(name = "OPEN_AT_STARTUP")
    boolean openAtStartup;
}
