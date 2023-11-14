package model;

import java.io.Serializable;

/**
 * The Book class represents a book in the library, with a title, author, available quantity, and total quantity.
 */
public class Book implements Serializable {
    
    private String title;
    private String author;
    private int availableQuantity;
    private int totalQuantity;

    /**
     * Creates a new Book object with the given title, author, available quantity, and total quantity.
     * @param title the title of the book
     * @param author the author of the book
     * @param availableQuantity the number of available copies of the book
     * @param totalQuantity the total number of copies of the book
     */
    public Book(String title, String author, int availableQuantity, int totalQuantity) {
        this.title = title;
        this.author = author;
        this.availableQuantity = availableQuantity;
        this.totalQuantity = totalQuantity;
    }

    /**
     * Returns the title of the book.
     * @return the title of the book
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Returns the author of the book.
     * @return the author of the book
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Returns the number of available copies of the book.
     * @return the number of available copies of the book
     */
    public int getAvailableQuantity() {
        return this.availableQuantity;
    }

    /**
     * Returns the total number of copies of the book.
     * @return the total number of copies of the book
     */
    public int getTotalQuantity() {
        return this.totalQuantity;
    }

    /**
     * Sets the number of available copies of the book.
     * @param quantity the new number of available copies of the book
     */
    public void setAvailableQuantity(int quantity) {
        this.availableQuantity = quantity;
    }

    /**
     * Sets the total number of copies of the book.
     * @param quantity the new total number of copies of the book
     */
    public void setTotalQuantity(int quantity) {
        this.totalQuantity = quantity;
    }

    /**
     * Returns true if the book is available (i.e. there is at least one available copy), false otherwise.
     * @return true if the book is available, false otherwise
     */
    public boolean isAvailable() {
        return this.availableQuantity > 0;
    }
}
