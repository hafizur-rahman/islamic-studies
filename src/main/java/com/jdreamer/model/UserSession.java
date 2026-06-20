package com.jdreamer.model;


import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "USER_SESSION")
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

    @Column(name = "PAGE_COUNT")
    int pageCount;

    @Column(name = "SIDE")
    int side = 0;

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public long getAccessedAt() {
        return accessedAt;
    }

    public void setAccessedAt(long accessedAt) {
        this.accessedAt = accessedAt;
    }

    public float getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(float zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public boolean isOpenAtStartup() {
        return openAtStartup;
    }

    public void setOpenAtStartup(boolean openAtStartup) {
        this.openAtStartup = openAtStartup;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserSession session)) return false;
        return bookId == session.bookId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bookId);
    }
}
