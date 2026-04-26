package com.jdreamer.service;

import com.jdreamer.model.Book;

import java.util.ArrayList;

public interface BookService {
    ArrayList<Book> findAllBooks();

    Book findBook(int id);
}
