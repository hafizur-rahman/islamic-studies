package com.jdreamer.service;

import com.jdreamer.model.Book;
import com.jdreamer.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class BookServiceImpl implements BookService {
    @Autowired
    BookRepository bookRepository;

    @Override
    public ArrayList<Book> findAllBooks() {
        return (ArrayList<Book>) bookRepository.findAll();
    }

    @Override
    public Book findBookById(int id) {
        return bookRepository.findById(id).orElse(null);
    }
}
