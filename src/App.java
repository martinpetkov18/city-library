import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

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
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        Properties prop = new Properties();
        InputStream input = null;
        Persistency persistency = null;

        try {
            input = new FileInputStream("config.properties");
            prop.load(input);

            String persistenceType = prop.getProperty("persistency");

            if (persistenceType.equals("db")) {
                persistency = new DBPersistency();
            } else if (persistenceType.equals("file")) {
                persistency = new FilePersistency();
            } else {
                throw new IllegalArgumentException("Invalid persistency type in config.properties. Only 'db' and 'file' are allowed.");
            }

            View view = new ConsoleView();
            LibraryController controller = new LibraryController(persistency, view);
            controller.runLibraryController();

        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("An I/O error occurred.");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (persistency != null) {
                persistency.close();
            }
        }
    }
}