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

    public DBPersistency(String dbURL, String userName, String password) throws SQLException {
        this.connection = DriverManager.getConnection(dbURL, userName, password);
    }

    @Override
    public void saveData(List<?> list, String tableName) throws SQLException {
        if (tableName.equals("Books")) {
            String updateQuery = "UPDATE Books SET availableQuantity = ?, totalQuantity = ? WHERE title = ? AND author = ?";
            String insertQuery = "INSERT INTO Books (title, author, availableQuantity, totalQuantity) SELECT ?,?,?,? FROM dual WHERE NOT EXISTS (SELECT * FROM Books WHERE title = ? AND author = ?)";
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
            String updateQuery = "UPDATE Readers SET borrowedBooks = ? WHERE name = ?";
            String insertQuery = "INSERT INTO Readers (name, borrowedBooks) SELECT ?,? FROM dual WHERE NOT EXISTS (SELECT * FROM Readers WHERE name = ?)";
            for (Object obj : list) {
                if (obj instanceof Reader) {
                    Reader reader = (Reader) obj;
                    
                    // First try to update existing reader
                    PreparedStatement updatePs = connection.prepareStatement(updateQuery);
                    updatePs.setString(1, reader.getBorrowedBooks().toString());
                    updatePs.setString(2, reader.getName());
                    int updated = updatePs.executeUpdate();

                    // If reader doesn't exist, insert new reader
                    if (updated == 0) {
                        PreparedStatement insertPs = connection.prepareStatement(insertQuery);
                        insertPs.setString(1, reader.getName());
                        insertPs.setString(2, reader.getBorrowedBooks().toString());
                        insertPs.setString(3, reader.getName());
                        insertPs.executeUpdate();
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
        if(tableName.equals("Books")) {
            resultSet = connection.createStatement().executeQuery("SELECT * FROM Books");
            List<Book> books = new ArrayList<>();
            while (resultSet.next()) {
                // assuming these are the names of your columns in Books table.
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                int availableQuantity = resultSet.getInt("availableQuantity");
                int totalQuantity = resultSet.getInt("totalQuantity");
                books.add(new Book(title, author, availableQuantity, totalQuantity));
            }
            return books;
        }

        // implement similar for Readers, based on your class structure and database schema.

        return null;
    }
    
    protected void finalize() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
            }
        }
    }
    // protected void finalize() throws Throwable {
    //     if (connection != null) {
    //         connection.close();
    //     }
    //     super.finalize();
    // }
}
