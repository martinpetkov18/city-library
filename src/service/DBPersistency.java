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

        String createBooksTable = "CREATE TABLE IF NOT EXISTS Books (" +
            "title VARCHAR(100) NOT NULL, " + 
            "author VARCHAR(100) NOT NULL, " + 
            "availableQuantity INT NOT NULL, " +
            "totalQuantity INT NOT NULL, " + 
            "PRIMARY KEY (title, author))";
        PreparedStatement ps = connection.prepareStatement(createBooksTable);
        ps.execute();

        String createReadersTable = "CREATE TABLE IF NOT EXISTS Readers (" +
            "name VARCHAR(100) NOT NULL PRIMARY KEY)";
        ps = connection.prepareStatement(createReadersTable);
        ps.execute();

        String createBorrowedBooksTable = "CREATE TABLE IF NOT EXISTS BorrowedBooks (" +
            "name VARCHAR(100) NOT NULL, " +
            "title VARCHAR(100) NOT NULL, " +
            "author VARCHAR(100) NOT NULL, " +
            "FOREIGN KEY (name) REFERENCES Readers(name)," +
            "FOREIGN KEY (title, author) REFERENCES Books(title, author))";
        ps = connection.prepareStatement(createBorrowedBooksTable);
        ps.execute();

        // Delete data from tables
        String deleteBorrowedBooks = "DELETE FROM BorrowedBooks";
        PreparedStatement deleteStatement = connection.prepareStatement(deleteBorrowedBooks);
        deleteStatement.execute();

        String deleteReaders = "DELETE FROM Readers";
        deleteStatement = connection.prepareStatement(deleteReaders);
        deleteStatement.execute();

        String deleteBooks = "DELETE FROM Books";
        deleteStatement = connection.prepareStatement(deleteBooks);
        deleteStatement.execute();

        // Add readers
        String insertReaders = "INSERT IGNORE INTO Readers (name, borrowedBooks) VALUES (?, '')";
        String[] readerNames = {"John", "Jane", "Alice", "Bob", "Charlie", "David", "Emma", "Frank", "Grace", "Harry"};
        for (String readerName : readerNames) {
            PreparedStatement preparedStatement = connection.prepareStatement(insertReaders);
            preparedStatement.setString(1, readerName);
            preparedStatement.executeUpdate();
        }

        // Add books
        String insertBooks = "INSERT IGNORE INTO Books (title, author, availableQuantity, totalQuantity) VALUES (?, ?, 1, 1)";
        String[][] booksData = {
            {"To Kill a Mockingbird", "Harper Lee"},
            {"1984", "George Orwell"},
            {"Harry Potter and Philosopher's Stone", "J.K. Rowling"},
            {"The Lord of the Rings", "J.R.R. Tolkien"},
            {"The Great Gatsby", "F. Scott Fitzgerald"},
            {"Pride and Prejudice", "Jane Austen"},
            {"The Catcher in the Rye", "J.D. Salinger"},
            {"The Hobbit", "J.R.R. Tolkien"},
            {"Moby-Dick", "Herman Melville"},
            {"War and Peace", "Leo Tolstoy"},
            {"Ulysses", "James Joyce"},
            {"The Odyssey", "Homer"},
            {"Crime and Punishment", "Fyodor Dostoevsky"},
            {"A Tale of Two Cities", "Charles Dickens"},
            {"The Shining", "Stephen King"},
            {"The Da Vinci Code", "Dan Brown"},
            {"Les Miserables", "Victor Hugo"},
            {"The Little Prince", "Antoine de Saint-Exup?ry"},
            {"Animal Farm", "George Orwell"},
            {"A Game of Thrones", "George R.R. Martin"}
        };
        for (String[] bookData : booksData) {
            PreparedStatement preparedStatement = connection.prepareStatement(insertBooks);
            preparedStatement.setString(1, bookData[0]);
            preparedStatement.setString(2, bookData[1]);
            preparedStatement.executeUpdate();
        }

        // Add to BorrowedBooks
        String insertBorrowed = "INSERT IGNORE INTO BorrowedBooks (name, title, author) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE name=name";
        String[][] borrowedData = {
                {"John", "To Kill a Mockingbird", "Harper Lee"}, 
                {"Jane", "1984", "George Orwell"},
                {"Alice", "Harry Potter and Philosopher's Stone", "J.K. Rowling"},
                {"Bob", "The Lord of the Rings", "J.R.R. Tolkien"},
                {"Charlie", "The Great Gatsby", "F. Scott Fitzgerald"}
        };
        for (String[] borrowedPair : borrowedData) {
            PreparedStatement preparedStatement = connection.prepareStatement(insertBorrowed);
            preparedStatement.setString(1, borrowedPair[0]);
            preparedStatement.setString(2, borrowedPair[1]);
            preparedStatement.setString(3, borrowedPair[2]);
            preparedStatement.executeUpdate();
        } 
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
            String updateQuery = "UPDATE Readers SET borrowedBooks = ? WHERE name = ?";
            String insertQuery = "INSERT INTO Readers (name, borrowedBooks) SELECT ?,? FROM dual WHERE NOT EXISTS (SELECT * FROM Readers WHERE name = ?)";
            for (Object obj : list) {
                if (obj instanceof Reader) {
                    Reader reader = (Reader) obj;
                    
                    PreparedStatement updatePs = connection.prepareStatement(updateQuery);
                    updatePs.setString(1, reader.getBorrowedBooks().toString());
                    updatePs.setString(2, reader.getName());
                    int updated = updatePs.executeUpdate();

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
                    System.out.println("Borrowed book: " + book.getTitle() + " by " + book.getAuthor());
                    System.out.println(book.getAvailableQuantity());
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
