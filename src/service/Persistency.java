package service;

import java.io.IOException;
import java.util.List;

/**
 * This interface defines the methods for saving and loading data from a file.
 */
public interface Persistency {
    /**
     * Saves a list of objects to a file with the given filename.
     * @param list The list of objects to be saved.
     * @param filename The name of the file to save the data to.
     * @throws IOException If there is an error writing to the file.
     */
    void saveData(List<?> list, String filename) throws IOException;
    
    /**
     * Loads a list of objects from a file with the given filename.
     * @param filename The name of the file to load the data from.
     * @return The list of objects loaded from the file.
     * @throws IOException If there is an error reading from the file.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    List<?> loadData(String filename) throws IOException, ClassNotFoundException;
}