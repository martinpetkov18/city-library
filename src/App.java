import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

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
     * @throws SQLException
     */
    public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {

        // Persistency filePersistency = new FilePersistency();
        Persistency dbPersistency = new DBPersistency("jdbc:mysql://localhost:3306/citylibrary");
        View view = new ConsoleView();
        LibraryController controller = new LibraryController(dbPersistency, view);

        try {
            controller.runLibraryController();
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("An I/O error occurred.");
        } finally {
            dbPersistency.close();
        }
    }
}