package com.citylibrary.citylibrary.controller;

import com.citylibrary.citylibrary.model.Book;
import com.citylibrary.citylibrary.model.Reader;
import com.citylibrary.citylibrary.service.LibraryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping("/register-reader")
    public String registerReader(@RequestParam String name) {
        libraryService.registerReader(name);
        return "Reader registered successfully";
    }

    @GetMapping("/readers")
    public List<Reader> getAllReaders() {
        return libraryService.getAllReaders();
    }

    @PostMapping("/add-book")
    public String addBook(@RequestParam String title, @RequestParam String author) {
        libraryService.addBook(title, author);
        return "Book added successfully";
    }

    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return libraryService.getAllBooks();
    }

    // Add this for fetching available books
    @GetMapping("/books/available")
    public List<Book> getAvailableBooks() {
        return libraryService.getAvailableBooks(); 
    }

    // Add this for fetching a specific reader's books
    @GetMapping("/books/reader")
    public List<Book> getReaderBooks(@RequestParam String readerName) {
        return libraryService.getReaderBooks(readerName); 
    }

    // Add this for fetching sorted books
    @GetMapping("/books/sorted")
    public List<Book> getSortedBooks(@RequestParam String sortKey) {
        return libraryService.getSortedBooks(sortKey); 
    }

    @GetMapping("/search-books")
    public List<Book> searchBooks(@RequestParam String query, @RequestParam String type) {
        if ("title".equalsIgnoreCase(type)) {
            return libraryService.searchBooksByTitle(query);
        } else if ("author".equalsIgnoreCase(type)) {
            return libraryService.searchBooksByAuthor(query);
        } else {
            throw new IllegalArgumentException("Invalid search type. Please choose 'title' or 'author'.");
        }
    }

    @PutMapping("/borrow-book")
    public String markBookAsBorrowed(@RequestParam String readerName, @RequestParam String bookTitle) {
        libraryService.borrowBook(readerName, bookTitle);
        return "Borrowing of book succeeded";
    }

    @PutMapping("/return-book")
    public String markBookAsReturned(@RequestParam String readerName, @RequestParam String bookTitle) {
        libraryService.returnBook(readerName, bookTitle);
        return "Returning of book succeeded";
    }
}