package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Catalog class represents a collection of books in the library.
 */
public class Catalog implements Serializable {
    private List<Book> books;

    /**
     * Constructs an empty catalog.
     */
    public Catalog() {
        books = new ArrayList<>();
    } 

    /**
     * Returns the list of books in the catalog.
     * @return the list of books in the catalog
     */
    public List<Book> getBooks() {
        return books;
    }

    /**
     * Adds a book to the catalog.
     * @param book the book to add to the catalog
     */
    public void addBook(Book book) {
        books.add(book);
    }
}