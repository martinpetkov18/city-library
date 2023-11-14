package view;

/**
 * The View interface defines the methods that a view implementation should provide.
 */
public interface View {
    
    /**
     * Sets the locale of the view based on the properties file.
     */
    void setLocaleFromProperties();
    
    /**
     * Displays the main menu of the application.
     */
    void displayMenu();
    
    /**
     * Displays the catalog menu of the application.
     */
    void displayCatalogMenu();
    
    /**
     * Displays a message based on the given key from the properties file.
     * @param key the key of the message to display
     */
    void displayPropertiesMessage(String key);
    
    /**
     * Displays the given message to the user.
     * @param message the message to display
     */
    void displayMessage(String message);
    
    /**
     * Prompts the user for a command and returns the input.
     * @return the user's input
     */
    String promptForUserCommand();
    
    /**
     * Prompts the user for a reader name and returns the input.
     * @return the user's input
     */
    String promptForReaderName();
    
    /**
     * Prompts the user for a book title and returns the input.
     * @return the user's input
     */
    String promptForBookTitle();
    
    /**
     * Prompts the user for a book author and returns the input.
     * @return the user's input
     */
    String promptForBookAuthor();
    
    /**
     * Prompts the user for a search query and returns the input.
     * @return the user's input
     */
    String promptForSearchQuery();
    
    /**
     * Prompts the user to choose between searching by title or author and returns the input.
     * @return the user's input
     */
    String promptForTitleOrAuthor();
    
    /**
     * Prompts the user to change the language and returns the input.
     * @return the user's input
     */
    String promptForLanguageChange();
    
    /**
     * Prompts the user for a book index and returns the input.
     * @return the user's input
     */
    String promptForBookIndex();
}