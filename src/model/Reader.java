package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reader who can borrow books from the library.
 */
public class Reader implements Serializable {
    private String name;
    private List<Book> borrowedBooks;

    /**
     * Constructs a new Reader object with the given name.
     * @param name the name of the reader
     */
    public Reader(String name) {
        this.name = name;
        this.borrowedBooks = new ArrayList<>();
    }

    /**
     * Returns the name of the reader.
     * @return the name of the reader
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a list of books that the reader has borrowed.
     * @return a list of books that the reader has borrowed
     */
    public List<Book> getBorrowedBooks() {
        return this.borrowedBooks;
    }

    /**
     * Borrows a book from the library and adds it to the reader's list of borrowed books.
     * Also updates the available quantity of the book.
     * @param book the book to be borrowed
     */
    public void borrowBook(Book book) {
        borrowedBooks.add(book);
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
    }

    /**
     * Returns a book to the library and removes it from the reader's list of borrowed books.
     * Also updates the available quantity of the book.
     * @param book the book to be returned
     */
    public void returnBook(Book book) {
        borrowedBooks.remove(book);
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
    }
}