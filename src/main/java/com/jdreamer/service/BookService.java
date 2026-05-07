package com.jdreamer.service;

import com.jdreamer.model.*;

import java.util.List;

public interface BookService {
    void save(Book newBook);

    void save(Noun noun);

    void save(Verb verb);

    void save(MediaLink verb);

    void saveSession(UserSession session);

    void saveNouns(List<Noun> nouns);

    void saveVerbs(List<Verb> verbs);

    void saveMediaLinks(List<MediaLink> videos);

    Book findBookById(int id);

    Book findBookByFilePath(String filePath);

    UserSession findUserSessionByBookId(int bookId);

    List<Noun> findNounsByBookIdAndPageId(int bookId, int pageId);

    List<Verb> findVerbsByBookIdAndPageId(int bookId, int pageId);

    List<UserSession> findUserSessionsByOpenAtStartup();

    List<Book> findAllBooksByBookIds(List<Integer> bookIds);

    List<MediaLink> findMediaLinksByBookIdAndPageId(int bookId, int pageId);

}
