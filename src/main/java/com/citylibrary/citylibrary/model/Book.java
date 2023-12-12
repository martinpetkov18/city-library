package com.citylibrary.citylibrary.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "Books")
public class Book implements Serializable {

    @EmbeddedId
    private BookId id;

    @Column(name = "availableQuantity")
    private int availableQuantity;

    @Column(name = "totalQuantity")
    private int totalQuantity;

    public Book() {}

    public Book(BookId id, int availableQuantity, int totalQuantity) {
        this.id = id;
        this.availableQuantity = availableQuantity;
        this.totalQuantity = totalQuantity;
    }

    public BookId getId() {
        return id;
    }

    public void setId(BookId id) {
        this.id = id;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public boolean isAvailable() {
        return availableQuantity > 0;
    }

    @Embeddable
    public static class BookId implements Serializable {
        @Column(name = "title")
        private String title;

        @Column(name = "author")
        private String author;

        public BookId() {}

        public BookId(String title, String author) {
            this.title = title;
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BookId)) return false;

            BookId bookId = (BookId) o;

            if (getTitle() != null ? !getTitle().equals(bookId.getTitle()) : bookId.getTitle() != null) return false;
            return getAuthor() != null ? getAuthor().equals(bookId.getAuthor()) : bookId.getAuthor() == null;
        }

        @Override
        public int hashCode() {
            int result = getTitle() != null ? getTitle().hashCode() : 0;
            result = 31 * result + (getAuthor() != null ? getAuthor().hashCode() : 0);
            return result;
        }
    }
}