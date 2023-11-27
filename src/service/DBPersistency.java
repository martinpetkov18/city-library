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

    private Connection connection;

    public DBPersistency(String dbURL) throws SQLException {
        String userName = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        this.connection = DriverManager.getConnection(dbURL, userName, password); 
    }

    @Override
    public void saveData(List<?> list, String tableName) throws SQLException {
        if (tableName.equals("Books")) {
            String updateQuery = "UPDATE Books SET availableQuantity = ?, totalQuantity = ? WHERE title = ? AND author = ?";
            String insertQuery = "INSERT IGNORE INTO Books (title, author, availableQuantity, totalQuantity) SELECT ?,?,?,? FROM dual WHERE NOT EXISTS (SELECT * FROM Books WHERE title = ? AND author = ?)";
            for (Object obj : list) {
                if (obj instanceof Book) {
                    Book book = (Book) obj;
                    
                    // First try to update existing book
                    PreparedStatement updatePs = connection.prepareStatement(updateQuery);
                    updatePs.setInt(1, book.getAvailableQuantity());
                    updatePs.setInt(2, book.getTotalQuantity());
                    updatePs.setString(3, book.getTitle());
                    updatePs.setString(4, book.getAuthor());
                    int updated = updatePs.executeUpdate();

                    // If book doesn't exist, insert new book
                    if (updated == 0) {
                        PreparedStatement insertPs = connection.prepareStatement(insertQuery);
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
        } else if (tableName.equals("Readers")) {
            String insertBorrowedBooksQuery = "INSERT INTO BorrowedBooks (name, title, author) SELECT ?,?,? FROM dual WHERE NOT EXISTS (SELECT * FROM BorrowedBooks WHERE name = ? AND title = ? AND author = ?)";
            String updateReaderQuery = "INSERT INTO Readers (name) SELECT ? FROM dual WHERE NOT EXISTS (SELECT * FROM Readers WHERE name = ?)";
            for (Object obj : list) {
                if (obj instanceof Reader) {
                    Reader reader = (Reader) obj;

                    PreparedStatement updateReaderPs = connection.prepareStatement(updateReaderQuery);
                    updateReaderPs.setString(1, reader.getName());
                    updateReaderPs.setString(2, reader.getName());
                    updateReaderPs.executeUpdate();

                    for (Book borrowedBook : reader.getBorrowedBooks()) {
                        PreparedStatement insertBorrowedBooksPs = connection.prepareStatement(insertBorrowedBooksQuery);
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
        } else {
            throw new IllegalArgumentException("Unknown table: " + tableName);
        }
    }

    @Override
    public List<?> loadData(String tableName) throws SQLException {
        ResultSet resultSet;
        if (tableName.equalsIgnoreCase("Books")) {
            resultSet = connection.createStatement().executeQuery("SELECT * FROM Books");
            List<Book> books = new ArrayList<>();
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                int availableQuantity = resultSet.getInt("availableQuantity");
                int totalQuantity = resultSet.getInt("totalQuantity");
                books.add(new Book(title, author, availableQuantity, totalQuantity));
            }
            return books;
        }
        
        else if (tableName.equalsIgnoreCase("Readers")) {
            resultSet = connection.createStatement().executeQuery("SELECT * FROM Readers");
            List<Reader> readers = new ArrayList<>();
            while (resultSet.next()) {
                String name = resultSet.getString("name");

                PreparedStatement ps = connection.prepareStatement("SELECT title, author FROM BorrowedBooks WHERE name = ?");
                ps.setString(1, name);
                ResultSet borrowedBooksResultSet = ps.executeQuery();

                List<Book> borrowedBooks = new ArrayList<>();
                while(borrowedBooksResultSet.next()) {
                    String title = borrowedBooksResultSet.getString("title");
                    String author = borrowedBooksResultSet.getString("author");

                    PreparedStatement bookPs = connection.prepareStatement("SELECT availableQuantity, totalQuantity FROM Books WHERE title = ? AND author = ?");
                    bookPs.setString(1, title);
                    bookPs.setString(2, author);
                    ResultSet bookResultSet = bookPs.executeQuery();

                    if(bookResultSet.next()) {
                        int availableQuantity = bookResultSet.getInt("availableQuantity");
                        int totalQuantity = bookResultSet.getInt("totalQuantity");
                        borrowedBooks.add(new Book(title, author, availableQuantity, totalQuantity));
                    }
                }

                // Create Reader object and borrow books
                Reader reader = new Reader(name);
                for (Book book : borrowedBooks) {
                    reader.borrowBook(book);
                }
                readers.add(reader);
            }
            return readers;
        }

        return null;
    }
    
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
