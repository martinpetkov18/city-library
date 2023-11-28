package service;

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

    private static final String BOOKS_TABLE = "Books";
    private static final String READERS_TABLE = "Readers";
    private Connection connection;

    public DBPersistency(String dbURL) throws SQLException {
        this.connection = DriverManager.getConnection(dbURL, System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"));
    }

    @Override
    public void saveData(List<?> list, String tableName) throws SQLException {
        if (tableName.equals(BOOKS_TABLE)) {
            handleBooks(list);
        } else if (tableName.equals(READERS_TABLE)) {
            handleReaders(list);
        } else {
            throw new IllegalArgumentException("Unknown table: " + tableName);
        }
    }

    private void handleBooks(List<?> list) throws SQLException {
        String updateQuery = "UPDATE Books SET availableQuantity = ?, totalQuantity = ? WHERE title = ? AND author = ?";
        String insertQuery = "INSERT IGNORE INTO Books (title, author, availableQuantity, totalQuantity) SELECT ?,?,?,? FROM dual WHERE NOT EXISTS (SELECT * FROM Books WHERE title = ? AND author = ?)";
        PreparedStatement updatePs = connection.prepareStatement(updateQuery);
        PreparedStatement insertPs = connection.prepareStatement(insertQuery);

        for (Object obj : list) {
            if (obj instanceof Book) {
                Book book = (Book) obj;

                updatePs.setInt(1, book.getAvailableQuantity());
                updatePs.setInt(2, book.getTotalQuantity());
                updatePs.setString(3, book.getTitle());
                updatePs.setString(4, book.getAuthor());
                int updated = updatePs.executeUpdate();

                if (updated == 0) {
                    insertPs.setString(1, book.getTitle());
                    insertPs.setString(2, book.getAuthor());
                    insertPs.setInt(3, book.getAvailableQuantity());
                    insertPs.setInt(4, book.getTotalQuantity());
                    insertPs.setString(5, book.getTitle());
                    insertPs.setString(6, book.getAuthor());
                    insertPs.executeUpdate();
                }
            }
        }
    }

    private void handleReaders(List<?> list) throws SQLException {
        String insertBorrowedBooksQuery = "INSERT INTO BorrowedBooks (name, title, author) SELECT ?,?,? FROM dual WHERE NOT EXISTS (SELECT * FROM BorrowedBooks WHERE name = ? AND title = ? AND author = ?)";
        String updateReaderQuery = "INSERT INTO Readers (name) SELECT ? FROM dual WHERE NOT EXISTS (SELECT * FROM Readers WHERE name = ?)";
        PreparedStatement updateReaderPs = connection.prepareStatement(updateReaderQuery);
        PreparedStatement insertBorrowedBooksPs = connection.prepareStatement(insertBorrowedBooksQuery);

        for (Object obj : list) {
            if (obj instanceof Reader) {
                Reader reader = (Reader) obj;

                updateReaderPs.setString(1, reader.getName());
                updateReaderPs.setString(2, reader.getName());
                updateReaderPs.executeUpdate();

                for (Book borrowedBook : reader.getBorrowedBooks()) {
                    insertBorrowedBooksPs.setString(1, reader.getName());
                    insertBorrowedBooksPs.setString(2, borrowedBook.getTitle());
                    insertBorrowedBooksPs.setString(3, borrowedBook.getAuthor());
                    insertBorrowedBooksPs.setString(4, reader.getName());
                    insertBorrowedBooksPs.setString(5, borrowedBook.getTitle());
                    insertBorrowedBooksPs.setString(6, borrowedBook.getAuthor());
                    insertBorrowedBooksPs.executeUpdate();
                }
            }
        }
    }

    @Override
    public List<?> loadData(String tableName) throws SQLException {
        if (tableName.equalsIgnoreCase(BOOKS_TABLE)) {
            return loadBooks();
        } else if (tableName.equalsIgnoreCase(READERS_TABLE)) {
           return loadReaders();
        }
        return null;
    }

    private List<Book> loadBooks() throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Books");
        List<Book> books = new ArrayList<>();
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

    private List<Reader> loadReaders() throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Readers");
        List<Reader> readers = new ArrayList<>();
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

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}