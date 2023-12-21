package service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Book;
import model.Reader;

/**
 * This class provides file-based implementation of the Persistency interface.
 * It allows saving and loading data to and from a file.
 */
public class FilePersistency implements Persistency {

    /**
     * Saves the given list of objects to the specified file.
     *
     * @param list     the list of objects to save
     * @param filename the name of the file to save to
     * @throws IOException if an I/O error occurs while saving the data
     */
    private void saveData(List<?> list, String filename) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(filename);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(list);
        out.close();
        fileOut.close();
    }

    @Override
    public void saveBooks(List<Book> books) throws IOException, SQLException {
        saveData(books, "books.txt");
    }

    @Override
    public void saveReaders(List<Reader> readers) throws IOException, SQLException {
        saveData(readers, "readers.txt");
    }

    private <T> List<T> loadData(String filename, Class<T> typeClass) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        List<?> data = (List<?>) in.readObject();
        List<T> result = new ArrayList<>(data.size());
        for (Object item : data) {
            if (typeClass.isInstance(item)) {
                result.add(typeClass.cast(item));
            }
        }
        in.close();
        fileIn.close();
        return result;
    }
    
    @Override
    public List<Book> loadBooks() throws IOException, ClassNotFoundException, SQLException {
        return loadData("books.txt", Book.class);
    }
    
    @Override
    public List<Reader> loadReaders() throws IOException, ClassNotFoundException, SQLException {
        return loadData("readers.txt", Reader.class);
    }

    @Override
    public String getType() {
        return "File";
    }

    @Override
    public void close() {}
}