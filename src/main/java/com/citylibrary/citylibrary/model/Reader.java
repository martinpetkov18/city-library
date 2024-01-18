package com.citylibrary.citylibrary.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "Readers")
public class Reader implements Serializable {

    @Id
    @Column(name = "name")
    private String name;

    @ManyToMany
    @JoinTable(
            name = "BorrowedBooks",
            joinColumns = @JoinColumn(name = "name"),
            inverseJoinColumns = {
                    @JoinColumn(name = "title", referencedColumnName = "title"),
                    @JoinColumn(name = "author", referencedColumnName = "author")
            }
    )
    private Set<Book> borrowedBooks = new HashSet<>();

    public Reader() {}

    public Reader(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    public void setBorrowedBooks(Set<Book> borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
    }

    public void borrowBook(Book book) {
        this.borrowedBooks.add(book);
    }

    public void returnBook(Book book) {
        this.borrowedBooks.remove(book);
    }

    public boolean hasBook(String bookTitle) {
        return this.borrowedBooks.stream().anyMatch(book -> book.getId().getTitle().equals(bookTitle));
    }
}