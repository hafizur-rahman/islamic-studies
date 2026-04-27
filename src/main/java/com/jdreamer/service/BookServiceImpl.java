package com.jdreamer.service;

import com.jdreamer.model.Book;
import com.jdreamer.model.Noun;
import com.jdreamer.model.UserSession;
import com.jdreamer.model.Verb;
import com.jdreamer.repository.BookRepository;
import com.jdreamer.repository.NounRepository;
import com.jdreamer.repository.UserSessionRepository;
import com.jdreamer.repository.VerbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {
    @Autowired
    BookRepository bookRepository;

    @Autowired
    NounRepository nounRepository;

    @Autowired
    VerbRepository verbRepository;

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
    public void save(Noun noun) {
        Optional<Noun> n = nounRepository.findBySingular(noun.getSingular());

        if (n.isPresent()) {
            noun.setId(n.get().getId());
        }

        nounRepository.save(noun);
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

    @Override
    public List<Noun> findNounsByBookIdAndPageId(int bookId, int pageId) {
        return nounRepository.findNounsByBookIdAndPageId(bookId, pageId);
    }

    @Override
    public void saveNouns(List<Noun> nouns) {
        nounRepository.saveAll(nouns);
    }

    @Override
    public void saveVerbs(List<Verb> verbs) {
        verbRepository.saveAll(verbs);
    }

    @Override
    public List<Verb> findVerbsByBookIdAndPageId(int bookId, int pageId) {
        return verbRepository.findNounsByBookIdAndPageId(bookId, pageId);
    }
}
