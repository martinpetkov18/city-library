package com.citylibrary.citylibrary.service;

import com.citylibrary.citylibrary.model.Book;
import com.citylibrary.citylibrary.model.Reader;
import com.citylibrary.citylibrary.repository.BookRepository;
import com.citylibrary.citylibrary.repository.ReaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;

    @Autowired
    public LibraryService(
            BookRepository bookRepository,
            ReaderRepository readerRepository) {
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
    }

    @Transactional
    public void registerReader(String name) {
        if (readerRepository.existsById(name)) {
            throw new IllegalArgumentException("Reader already exists.");
        }
        Reader reader = new Reader(name);
        readerRepository.save(reader);
    }

    public List<Reader> getAllReaders() {
        return readerRepository.findAll();
    }

    @Transactional
    public void addBook(String title, String author) {
        Book.BookId bookId = new Book.BookId(title, author);
        Book book = bookRepository.findById(bookId).orElse(null);

        if (book != null) {
            book.setTotalQuantity(book.getTotalQuantity() + 1);
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        } else {
            book = new Book(bookId, 1, 1);
        }
        bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> searchBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
    
    public List<Book> searchBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    @Transactional
    public void borrowBook(String readerName, String bookTitle, String bookAuthor) {
        Reader reader = readerRepository.findById(readerName)
                .orElseThrow(() -> new IllegalArgumentException("No reader found with name " + readerName));
        Book book = bookRepository.findById(new Book.BookId(bookTitle, bookAuthor))
                .orElseThrow(() -> new IllegalArgumentException("No book found with Title " + bookTitle + " and Author " + bookAuthor));

        if (book.isAvailable()) {
            reader.borrowBook(book);
            book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        } else {
            throw new IllegalStateException("Book is not available");
        }
    }

    @Transactional
    public void returnBook(String readerName, String bookTitle, String bookAuthor) {
        Reader reader = readerRepository.findById(readerName)
                .orElseThrow(() -> new IllegalArgumentException("No reader found with name " + readerName));
        Book book = bookRepository.findById(new Book.BookId(bookTitle, bookAuthor))
                .orElseThrow(() -> new IllegalArgumentException("No book found with Title " + bookTitle + " and Author " + bookAuthor));

        if (reader.getBorrowedBooks().contains(book)) {
            reader.returnBook(book);
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        } else {
            throw new IllegalStateException("Book was not borrowed by this reader");
        }
    }
}