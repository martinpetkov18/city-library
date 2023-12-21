package service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Book;
import model.Reader;

public class DBPersistency implements Persistency {

    private Connection connection;

    public DBPersistency() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/citylibrary", System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"));
    }

    @Override
    public List<Book> loadBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Books");
        while (resultSet.next()) {
            books.add(new Book(
                    resultSet.getString("title"),
                    resultSet.getString("author"),
                    resultSet.getInt("availableQuantity"),
                    resultSet.getInt("totalQuantity")
            ));
        }
        return books;
    }

    @Override
    public List<Reader> loadReaders() throws SQLException {
        List<Reader> readers = new ArrayList<>();
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Readers");
        while (resultSet.next()) {
            String name = resultSet.getString("name");
            ResultSet borrowedBooksResultSet = dealWithBorrowedBooks(name);
            
            List<Book> borrowedBooks = new ArrayList<>();
            while(borrowedBooksResultSet.next()) {
                borrowedBooks.add(new Book(
                        borrowedBooksResultSet.getString("title"),
                        borrowedBooksResultSet.getString("author"),
                        0, 0
                ));
            }

            Reader reader = new Reader(name);
            for (Book book : borrowedBooks) {
                reader.borrowBook(book);
            }
            readers.add(reader);
        }
        return readers;
    }

    private ResultSet dealWithBorrowedBooks(String name) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT title, author FROM BorrowedBooks WHERE name = ?");
        ps.setString(1, name);
        return ps.executeQuery();
    }

    @Override
    public void saveBooks(List<Book> books) throws IOException, SQLException {
        for (Book book : books) {
            String query = "INSERT INTO Books (title, author, availableQuantity, totalQuantity) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "availableQuantity = availableQuantity + VALUES(availableQuantity), " +
                        "totalQuantity = totalQuantity + VALUES(totalQuantity)";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, book.getTitle());
                ps.setString(2, book.getAuthor());
                ps.setInt(3, book.getAvailableQuantity());
                ps.setInt(4, book.getTotalQuantity());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void saveReaders(List<Reader> readers) throws IOException, SQLException {
        for (Reader reader : readers) {
            String insertReaderQuery = "INSERT INTO Readers (name) SELECT ? FROM dual WHERE NOT EXISTS (SELECT * FROM Readers WHERE name = ?)";
            PreparedStatement updateReaderPs = connection.prepareStatement(insertReaderQuery);
            
            updateReaderPs.setString(1, reader.getName());
            updateReaderPs.setString(2, reader.getName());
            updateReaderPs.executeUpdate();
        }
    }

    @Override
    public String getType() {
        return "DB";
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}