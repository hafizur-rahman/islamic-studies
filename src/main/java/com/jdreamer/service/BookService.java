package com.jdreamer.service;

import com.jdreamer.model.Book;
import com.jdreamer.model.UserSession;

import java.util.ArrayList;

public interface BookService {
    ArrayList<Book> findAllBooks();

    Book findBookById(int id);

    void save(Book newBook);

    Book findBookByFilePath(String filePath);

    UserSession findUserSessionByBookId(int bookId);

    void saveSession(UserSession session);
}
