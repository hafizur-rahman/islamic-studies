package com.jdreamer.service;

import com.jdreamer.model.Book;
import com.jdreamer.model.UserSession;
import com.jdreamer.repository.BookRepository;
import com.jdreamer.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class BookServiceImpl implements BookService {
    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserSessionRepository userSessionRepository;

    @Override
    public ArrayList<Book> findAllBooks() {
        return (ArrayList<Book>) bookRepository.findAll();
    }

    @Override
    public Book findBookById(int id) {
        return bookRepository.findById(id).orElse(null);
    }

    @Override
    public void save(Book newBook) {
        bookRepository.save(newBook);
    }

    @Override
    public Book findBookByFilePath(String filePath) {
        return bookRepository.findByFilePath(filePath);
    }

    @Override
    public UserSession findUserSessionByBookId(int bookId) {
        return userSessionRepository.findByBookId(bookId);
    }

    @Override
    public void saveSession(UserSession session) {
        userSessionRepository.save(session);
    }
}
