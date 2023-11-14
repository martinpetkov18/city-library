import java.io.FileNotFoundException;
import java.io.IOException;

import controller.*;
import service.*;
import view.*;

/**
 * The main class of the CityLibrary application.
 * It initializes the persistency, view, and controller objects, and runs the library controller.
 */
public class App {
    /**
     * The main method of the CityLibrary application.
     * Initializes the persistency, view, and controller objects, and runs the library controller.
     * @param args the command line arguments
     * @throws ClassNotFoundException if the specified class cannot be found
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        Persistency persistency = new FilePersistency();
        View view = new ConsoleView();
        LibraryController controller = new LibraryController(persistency, view);

        try {
            controller.runLibraryController();
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("An I/O error occurred.");
        }
    }
}