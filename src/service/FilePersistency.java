package service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    @Override
    public void saveData(List<?> list, String filename) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(filename);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(list);
        out.close();
        fileOut.close();
    }

    /**
     * Loads a list of objects from the specified file.
     *
     * @param filename the name of the file to load from
     * @return the list of objects loaded from the file
     * @throws IOException            if an I/O error occurs while loading the data
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    @Override
    public List<?> loadData(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        List<?> list = (ArrayList<?>) in.readObject();
        in.close();
        fileIn.close();
        return list;
    }

    @Override
    public void close() throws SQLException {}
}