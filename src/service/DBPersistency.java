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

    private static final String BOOKS_TABLE = "Books";
    private static final String READERS_TABLE = "Readers";
    private Connection connection;

    public DBPersistency(String dbURL) throws SQLException {
        this.connection = DriverManager.getConnection(dbURL, System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"));
    }

    @Override
    public void saveData(Object object, String tableName, Operation operation, Book book) throws SQLException {
        if (tableName.equals(BOOKS_TABLE) && object instanceof Book) {
            handleBooks((Book) object, operation);
        } else if (tableName.equals(READERS_TABLE) && object instanceof Reader) {
            handleReaders((Reader) object, operation, book);
        } else {
            throw new IllegalArgumentException("Unknown table: " + tableName + " or object: " + object.getClass().getName());
        }
    }

    private void handleBooks(Book book, Operation operation) throws SQLException {
        if (operation == Operation.ADD_BOOK) {
            insertBook(book);
        } else if (operation == Operation.UPDATE_BOOK) {
            updateBook(book);
        } else {
            throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }
    
    private void insertBook(Book book) throws SQLException {
        String query = "INSERT IGNORE INTO Books (title, author, availableQuantity, totalQuantity) SELECT ?,?,?,? FROM dual WHERE NOT EXISTS (SELECT * FROM Books WHERE title = ? AND author = ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setInt(3, book.getAvailableQuantity());
            ps.setInt(4, book.getTotalQuantity());
            ps.setString(5, book.getTitle());
            ps.setString(6, book.getAuthor());
            ps.executeUpdate();
        }
    }

    private void updateBook(Book book) throws SQLException {
        String query = "UPDATE Books SET availableQuantity = ?, totalQuantity = ? WHERE title = ? AND author = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, book.getAvailableQuantity());
            ps.setInt(2, book.getTotalQuantity());
            ps.setString(3, book.getTitle());
            ps.setString(4, book.getAuthor());
            ps.executeUpdate();
        }
    }

    private void handleReaders(Reader reader, Operation operation, Book book) throws SQLException {
        if (operation == Operation.ADD_READER) {
            insertReader(reader);
        } else if (operation == Operation.BORROW_BOOK) {
            insertBorrowedBooks(reader, book);
        } else if (operation == Operation.RETURN_BOOK) {
            deleteBorrowedBook(reader, book);
        } else {
            throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }
    
    private void insertReader(Reader reader) throws SQLException {
        String insertReaderQuery = "INSERT INTO Readers (name) SELECT ? FROM dual WHERE NOT EXISTS (SELECT * FROM Readers WHERE name = ?)";
        PreparedStatement updateReaderPs = connection.prepareStatement(insertReaderQuery);
        
        updateReaderPs.setString(1, reader.getName());
        updateReaderPs.setString(2, reader.getName());
        updateReaderPs.executeUpdate();
    }
    
    private void insertBorrowedBooks(Reader reader, Book borrowedBook) throws SQLException {
        String insertBorrowedBooksQuery = "INSERT INTO BorrowedBooks (name, title, author) SELECT ?,?,? FROM dual WHERE NOT EXISTS (SELECT * FROM BorrowedBooks WHERE name = ? AND title = ? AND author = ?)";
        PreparedStatement insertBorrowedBooksPs = connection.prepareStatement(insertBorrowedBooksQuery);
    
        insertBorrowedBooksPs.setString(1, reader.getName());
        insertBorrowedBooksPs.setString(2, borrowedBook.getTitle());
        insertBorrowedBooksPs.setString(3, borrowedBook.getAuthor());
        insertBorrowedBooksPs.setString(4, reader.getName());
        insertBorrowedBooksPs.setString(5, borrowedBook.getTitle());
        insertBorrowedBooksPs.setString(6, borrowedBook.getAuthor());
        insertBorrowedBooksPs.executeUpdate();
    }

    private void deleteBorrowedBook(Reader reader, Book borrowedBook) throws SQLException {
        String deleteQuery = "DELETE FROM BorrowedBooks WHERE name = ? AND title = ? AND author = ?";
        PreparedStatement deletePs = connection.prepareStatement(deleteQuery);

        deletePs.setString(1, reader.getName());
        deletePs.setString(2, borrowedBook.getTitle());
        deletePs.setString(3, borrowedBook.getAuthor());
        deletePs.executeUpdate();
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

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void saveData(List<?> list, String filename) throws IOException, SQLException {}
}