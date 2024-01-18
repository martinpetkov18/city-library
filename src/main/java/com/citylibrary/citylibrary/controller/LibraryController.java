package com.citylibrary.citylibrary.controller;

import com.citylibrary.citylibrary.model.Book;
import com.citylibrary.citylibrary.model.Reader;
import com.citylibrary.citylibrary.service.LibraryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
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
    public String registerReader(@RequestParam @NonNull String name) {
        libraryService.registerReader(name);
        return "Reader registered successfully";
    }

    @GetMapping("/readers")
    public List<Reader> getAllReaders() {
        return libraryService.getAllReaders();
    }

    @PostMapping("/add-book")
    public String addBook(@RequestParam String title, @RequestParam String author) {
        if (title.isEmpty() || author.isEmpty()) {
            return "Title and author cannot be empty.";
        } else {
            libraryService.addBook(title, author);
            return "Book added successfully";
        }
    }

    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return libraryService.getAllBooks();
    }

    @GetMapping("/books/available")
    public List<Book> getAvailableBooks() {
        return libraryService.getAvailableBooks(); 
    }

    @GetMapping("/books/reader")
    public List<Book> getReaderBooks(@RequestParam @NonNull String readerName) {
        return libraryService.getReaderBooks(readerName); 
    }

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
    public ResponseEntity<?> markBookAsBorrowed(@RequestParam String readerName, @RequestParam String bookTitle) {
        if (readerName.isEmpty() || bookTitle.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reader name and book title cannot be empty.");
        } else {
            try {
                libraryService.borrowBook(readerName, bookTitle);
                return ResponseEntity.ok("Borrowing of book succeeded");
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
            }
        }
    }

    @PutMapping("/return-book")
    public ResponseEntity<?> markBookAsReturned(@RequestParam String readerName, @RequestParam String bookTitle) {
        if (readerName.isEmpty() || bookTitle.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reader name and book title cannot be empty.");
        } else {
            try {
                libraryService.returnBook(readerName, bookTitle);
                return ResponseEntity.ok("Returning of book succeeded");
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
            } catch (IllegalStateException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
            }
        }
    }
}