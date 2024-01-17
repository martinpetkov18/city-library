package com.citylibrary.citylibrary.service;

import com.citylibrary.citylibrary.model.Book;
import com.citylibrary.citylibrary.model.Reader;
import com.citylibrary.citylibrary.repository.BookRepository;
import com.citylibrary.citylibrary.repository.ReaderRepository;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;

    public LibraryService(
            BookRepository bookRepository,
            ReaderRepository readerRepository) {
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
    }

    @Transactional
    public void registerReader(@NonNull String name) {
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

    public List<Book> getAvailableBooks() {
        return bookRepository.findAll().stream().filter(Book::isAvailable).collect(Collectors.toList());
    }

    public List<Book> getReaderBooks(@NonNull String readerName) {
        Reader reader = readerRepository.findById(readerName)
                .orElseThrow(() -> new IllegalArgumentException("No reader found with name " + readerName));
        return new ArrayList<>(reader.getBorrowedBooks());
    }

    public List<Book> getSortedBooks(String sortKey) {
        if ("title".equalsIgnoreCase(sortKey)) {
            return bookRepository.findAll(Sort.by("id.title"));
        } else if ("author".equalsIgnoreCase(sortKey)) {
            return bookRepository.findAll(Sort.by("id.author"));
        } else {
            throw new IllegalArgumentException("Invalid sort key. Please choose 'title' or 'author'.");
        }
    }

    public List<Book> searchBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
    
    public List<Book> searchBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    @Transactional
    public void borrowBook(@NonNull String readerName, String bookTitle) {
        Reader reader = readerRepository.findById(readerName)
                .orElseThrow(() -> new IllegalArgumentException("No reader found with name " + readerName));
        Book book = bookRepository.findById_Title(bookTitle)
                .orElseThrow(() -> new IllegalArgumentException("No book found with Title " + bookTitle));

        if (book.isAvailable()) {
            reader.borrowBook(book);
            book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        } else {
            throw new IllegalStateException("Book is not available");
        }
    }

    @Transactional
    public void returnBook(@NonNull String readerName, String bookTitle) {
        Reader reader = readerRepository.findById(readerName)
                .orElseThrow(() -> new IllegalArgumentException("No reader found with name " + readerName));
        Book book = bookRepository.findById_Title(bookTitle)
                .orElseThrow(() -> new IllegalArgumentException("No book found with Title " + bookTitle));

        if (reader.getBorrowedBooks().contains(book)) {
            reader.returnBook(book);
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        } else {
            throw new IllegalStateException("Book was not borrowed by this reader");
        }
    }
}