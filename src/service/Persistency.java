package service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import model.Book;
import model.Reader;

/**
 * This interface defines the methods for saving and loading data from a file.
 */
public interface Persistency {

    void saveBooks(List<Book> books) throws IOException, SQLException;

    void saveReaders(List<Reader> readers) throws IOException, SQLException;

    List<Book> loadBooks() throws IOException, ClassNotFoundException, SQLException;

    List<Reader> loadReaders() throws IOException, ClassNotFoundException, SQLException;

    String getType();

    void close() throws SQLException;
}