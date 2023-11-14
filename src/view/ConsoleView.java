package view;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * A class that implements the {@link View} interface and provides a console-based user interface for the City Library application.
 */
public class ConsoleView implements View {
    private static final String CONFIG_FILENAME = "config.properties";

    private Scanner scanner;
    private ResourceBundle messages;

    private PrintWriter writer;

    /**
     * Constructs a new instance of the {@code ConsoleView} class.
     * Initializes the scanner, writer, and messages fields.
     */
    public ConsoleView() {
        this.scanner = new Scanner(System.in);

        this.writer = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);

        setLocaleFromProperties();
    }

    /**
     * Reads the language setting from the config.properties file and sets the messages field accordingly.
     * If the file cannot be read, displays an error message.
     */
    public void setLocaleFromProperties() {
        Properties properties = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(CONFIG_FILENAME), StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException ex) {
            displayMessage("Error loading properties file");
        }

        String chosenLanguage = properties.getProperty("language", "en");
        Locale locale = ("bg".equals(chosenLanguage)) ? Locale.forLanguageTag("bg") : Locale.ENGLISH;
        this.messages = ResourceBundle.getBundle("view.messages", locale);
    }

    /**
     * Displays the main menu options to the user.
     */
    public void displayMenu() {
        System.out.println();
        displayPropertiesMessage("addReader");
        displayPropertiesMessage("showReaders");
        displayPropertiesMessage("addBook");
        displayPropertiesMessage("showCatalog");
        displayPropertiesMessage("searchBook");
        displayPropertiesMessage("markBorrowingBook");
        displayPropertiesMessage("markReturningBook");
        displayPropertiesMessage("changeLanguage");
        displayPropertiesMessage("exit");
    }

    /**
     * Displays the catalog menu options to the user.
     */
    public void displayCatalogMenu() {
        System.out.println();
        displayPropertiesMessage("showAllBooks");
        displayPropertiesMessage("showAvailableBooks");
        displayPropertiesMessage("showReadersBooks");
        displayPropertiesMessage("sortCatalog");
        displayPropertiesMessage("backToMainMenu");
    }

    /**
     * Displays the message corresponding to the given key in the messages resource bundle.
     * @param key the key of the message to display
     */
    public void displayPropertiesMessage(String key) {
        System.out.println(messages.getString(key));
    }

    /**
     * Displays the given message to the user.
     * @param message the message to display
     */
    public void displayMessage(String message) {
        System.out.println(message);
    }

    /**
     * Prompts the user to enter a command and returns the input.
     * @return the user's input
     */
    private String promptUserAndReturnInput(String message) {
        displayPropertiesMessage(message);
        return scanner.nextLine().trim();
    }

    /**
     * Prompts the user to enter a command and returns the input.
     * @return the user's input
     */
    public String promptForUserCommand() {
        return promptUserAndReturnInput("chooseOption");
    }
    
    /**
     * Prompts the user to enter a reader name and returns the input.
     * @return the user's input
     */
    public String promptForReaderName() {
        return promptUserAndReturnInput("enterName");
    }
    
    /**
     * Prompts the user to enter a book title and returns the input.
     * @return the user's input
     */
    public String promptForBookTitle() {
        return promptUserAndReturnInput("enterTitle");
    }
    
    /**
     * Prompts the user to enter a book author and returns the input.
     * @return the user's input
     */
    public String promptForBookAuthor() {
        return promptUserAndReturnInput("enterAuthor");
    }
    
    /**
     * Prompts the user to enter a search query and returns the input.
     * @return the user's input
     */
    public String promptForSearchQuery() {
        return promptUserAndReturnInput("enterSearch");
    }
    
    /**
     * Prompts the user to choose between searching by title or author and returns the input.
     * @return the user's input
     */
    public String promptForTitleOrAuthor() {
        return promptUserAndReturnInput("chooseTitleOrAuthor");
    }
    
    /**
     * Prompts the user to choose a language and returns the input.
     * @return the user's input
     */
    public String promptForLanguageChange() {
        return promptUserAndReturnInput("chooseLanguage");
    }

    /**
     * Prompts the user to choose a book index and returns the input.
     * @return the user's input
     */
    public String promptForBookIndex() {
        return promptUserAndReturnInput("chooseBookIndex");
    }
}