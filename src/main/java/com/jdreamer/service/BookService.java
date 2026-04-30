package com.jdreamer.service;

import com.jdreamer.model.Book;
import com.jdreamer.model.Noun;
import com.jdreamer.model.UserSession;
import com.jdreamer.model.Verb;

import java.util.ArrayList;
import java.util.List;

public interface BookService {
    ArrayList<Book> findAllBooks();

    Book findBookById(int id);

    void save(Book newBook);

    void save(Noun noun);

    void save(Verb verb);

    Book findBookByFilePath(String filePath);

    UserSession findUserSessionByBookId(int bookId);

    void saveSession(UserSession session);

    List<Noun> findNounsByBookIdAndPageId(int bookId, int pageId);

    void saveNouns(List<Noun> nouns);

    void saveVerbs(List<Verb> verbs);

    List<Verb> findVerbsByBookIdAndPageId(int bookId, int pageId);

    List<UserSession> findUserSessionsByOpenAtStartup();

    List<Book> findAllBooksByBookIds(List<Integer> bookIds);
}
