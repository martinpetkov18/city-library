package controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import model.Book;
import model.Catalog;
import model.Reader;
import service.Persistency;
import view.View;

/**
 * The LibraryController class is responsible for managing the library's catalog and readers.
 * It provides methods for registering readers, adding books to the catalog, displaying the catalog, searching for books,
 * borrowing and returning books, and changing the language of the user interface.
 * It also handles the persistence of data to files.
 */
public class LibraryController {
    // private static final String BOOKS_FILENAME = "books.txt";
    // private static final String READERS_FILENAME = "readers.txt";
    private static final String BOOKS_TABLE = "Books";
    private static final String READERS_TABLE = "Readers";
    private static final String CONFIG_FILENAME = "config.properties";

    private boolean exit = false;
    private Catalog catalog;
    private List<Reader> readers;
    private final Persistency persistency;
    private final View view;

    /**
     * This class represents a controller for the library system, responsible for managing the
     * communication between the view and the model. It initializes the persistency, view, catalog, and
     * readers, and loads the data from the persistency on creation.
     *
     * @param persistency the persistency object used to load and save data
     * @param view        the view object used to display information to the user
     * @throws ClassNotFoundException if the specified class cannot be found during deserialization
     * @throws IOException            if an I/O error occurs during deserialization
     */
    public LibraryController(Persistency persistency, View view) throws ClassNotFoundException, IOException {
        this.persistency = persistency;
        this.view = view;
        this.catalog = new Catalog();
        this.readers = new ArrayList<>();
        loadData();
    }

    /**
     * Saves the current state of the library by persisting the books catalog and readers list to files.
     */
    private void saveLibraryState() {
        saveBooksState(catalog.getBooks());
        saveReadersState(readers);
    }

    private void saveBooksState(List<Book> books) {
        try {
            persistency.saveData(books, BOOKS_TABLE);
        } catch (IOException e) {
            view.displayMessage("Failed to save book data: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void saveReadersState(List<Reader> readers) {
        try {
            persistency.saveData(readers, READERS_TABLE);
        } catch (IOException e) {
            view.displayMessage("Failed to save reader data: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads data from persistency and populates the catalog and readers list.
     * @throws IOException if an I/O error occurs while reading from the file.
     * @throws ClassNotFoundException if the class of a serialized object cannot be found.
     */
    private void loadData() throws IOException, ClassNotFoundException{
        try {
            List<?> loadedBooks = persistency.loadData(BOOKS_TABLE);
            List<Book> books = new ArrayList<>();
            for (Object obj : loadedBooks) {
                if (obj instanceof Book) {
                    books.add((Book) obj);
                } else {
                    throw new IllegalArgumentException("Loaded object is not an instance of Reader");
                }
            }
            books.forEach(catalog::addBook);

            List<?> loadedReaders = persistency.loadData(READERS_TABLE);
            List<Reader> readers = new ArrayList<>();
            for (Object obj : loadedReaders) {
                if (obj instanceof Reader) {
                    readers.add((Reader) obj);
                } else {
                    throw new IllegalArgumentException("Loaded object is not an instance of Reader");
                }
                
            }
            this.readers.addAll(readers);
        } catch (FileNotFoundException e) {
           view.displayMessage("Failed to load data.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the Library Controller.
     * Displays the menu and processes user commands until the exit flag is set to true.
     * @throws FileNotFoundException if the file is not found.
     * @throws IOException if an I/O error occurs.
     */
    public void runLibraryController() throws FileNotFoundException, IOException {
        while(!exit) {
            try {
                view.displayMenu();
                processCommand();
            } catch (Exception e) {
                view.displayMessage(e.getMessage());
            }
        }
    }

    /**
     * Processes the user command by prompting for input and executing the corresponding action.
     * The action is determined by the user input, which is matched against a set of predefined options.
     * If the user input is not recognized, an error message is displayed.
     */
    private void processCommand(){
        String userInput = view.promptForUserCommand();
        switch (userInput) {
            case "1": registerReader(); break;
            case "2": displayReaders(); break;
            case "3": addBook(); break;
            case "4": showCatalog(); break;
            case "5": searchBooks(); break;
            case "6": markBookAsBorrowed(); break;
            case "7": markBookAsReturned(); break;
            case "8": changeLanguage(); break;
            case "9": exit(); break;
            default: view.displayPropertiesMessage("invalidChoice");
        }
    }

    /**
     * Registers a new reader in the library system.
     * Prompts the user for the reader's name and checks if the reader already exists.
     * If the reader does not exist, adds the reader to the list of readers and saves the library state.
     * Otherwise, displays a message indicating that the reader already exists.
     */
    private void registerReader() {
        String readerName = view.promptForReaderName();
        Optional<Reader> existingReader = readers.stream()
            .filter(reader -> reader.getName().equalsIgnoreCase(readerName))
            .findFirst();

        if (existingReader.isPresent()) {
            view.displayPropertiesMessage("readerAlreadyExists");
        } else {
            Reader reader = new Reader(readerName);
            readers.add(reader);
            saveReadersState(Collections.singletonList(reader));
            view.displayPropertiesMessage("addedReader");
        }
    }

    /**
     * Displays the names of all readers in the library.
     */
    private void displayReaders() {
        view.displayMessage(readers.stream()
                    .map(Reader::getName)
                    .collect(Collectors.joining(", ")));
    }

    /**
     * Prompts the user for a book title and author, and either increments the available quantity of an existing book
     * or adds a new book to the catalog with a quantity of 1. Saves the library state and displays a message to the user.
     */
    private void addBook() {
        String title = view.promptForBookTitle();
        String author = view.promptForBookAuthor();

        Optional<Book> existingBook = catalog.getBooks().stream()
            .filter(book -> book.getTitle().equalsIgnoreCase(title) && book.getAuthor().equalsIgnoreCase(author))
            .findFirst();

        if (existingBook.isPresent()) {
            Book book = existingBook.get();
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
            book.setTotalQuantity(book.getTotalQuantity() + 1);
            saveBooksState(Collections.singletonList(book));
        } else {
            Book book = new Book(title, author, 1, 1);
            catalog.addBook(book);
            saveBooksState(Collections.singletonList(book));
        }

        view.displayPropertiesMessage("addedBook");
    }

    /**
     * Displays the catalog menu and prompts the user for input. 
     * Depending on the user's input, it calls one of the following methods:
     * showAllBooks(), showAvailableBooks(), showReadersBooks(), showSortedCatalog().
     * If the user enters an invalid input, it displays an error message.
     */
    private void showCatalog() {
        view.displayCatalogMenu();
        String userInput = view.promptForUserCommand();
        switch (userInput) {
            case "1": showAllBooks(); break;
            case "2": showAvailableBooks(); break;
            case "3": showReadersBooks(); break;
            case "4": showSortedCatalog(); break;
            case "5": break;
            default: view.displayPropertiesMessage("invalidChoice");
        }
    }

    /**
     * Displays all books in the library catalog along with their availability status.
     */
    private void showAllBooks() {
        view.displayMessage(catalog.getBooks().stream()
                    .map(book -> book.getTitle() + " (Available: " + book.getAvailableQuantity() + ")")
                    .collect(Collectors.joining(", ")));
    }

    /**
     * Displays the titles of all available books in the library catalog.
     */
    private void showAvailableBooks() {
        view.displayMessage(catalog.getBooks().stream()
                    .filter(book -> book.getAvailableQuantity() > 0)
                    .map(book -> book.getTitle() + " (Available: " + book.getAvailableQuantity() + ")")
                    .collect(Collectors.joining(", ")));
    }

    /**
     * Prompts the user for a reader's name, finds the reader in the list of readers, and displays the titles of the books borrowed by the reader.
     * @throws IllegalArgumentException if no reader with the given name is found.
     */
    private void showReadersBooks() {
        String inputName = view.promptForReaderName();
        Reader reader = readers.stream()
                    .filter(r -> r.getName().equalsIgnoreCase(inputName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No such reader found."));
        view.displayMessage(reader.getBorrowedBooks().stream()
                    .map(Book::getTitle)
                    .collect(Collectors.joining(", ")));
    }

    /**
     * Displays the sorted catalog based on user input of either title or author.
     * If user input is "title", the catalog is sorted by book title and displayed.
     * If user input is "author", the catalog is sorted by book author and displayed.
     * If user input is neither "title" nor "author", an error message is displayed.
     */
    private void showSortedCatalog() {
        String userInput = view.promptForTitleOrAuthor();

        if (userInput.equalsIgnoreCase("title")) {
            view.displayMessage(catalog.getBooks().stream()
                    .sorted(Comparator.comparing(Book::getTitle))
                    .map(Book::getTitle)
                    .collect(Collectors.joining(", ")));
        } else if (userInput.equalsIgnoreCase("author")) {
            view.displayMessage(catalog.getBooks().stream()
                    .sorted(Comparator.comparing(Book::getAuthor))
                    .map(book -> book.getAuthor() + " - " + book.getTitle())
                    .collect(Collectors.joining(", ")));
        } else {
            view.displayPropertiesMessage("invalidChoice");
        }
    }
    
    /**
     * This method prompts the user for a search query and searches for books in the catalog based on the user's input.
     * If the user chooses to search by title, the method filters the books by title and displays the titles of the matching books.
     * If the user chooses to search by author, the method filters the books by author and displays the author and title of the matching books.
     * If the user enters an invalid choice, the method displays an error message.
     */
    private void searchBooks() {
        String userInput = view.promptForTitleOrAuthor();
        String searchQuery = view.promptForSearchQuery().toLowerCase();

        if (userInput.equalsIgnoreCase("1")) {
            view.displayMessage(catalog.getBooks().stream()
                    .filter(book -> book.getTitle().toLowerCase().contains(searchQuery))
                    .map(Book::getTitle)
                    .collect(Collectors.joining(", ")));
        } else if (userInput.equalsIgnoreCase("2")) {
            view.displayMessage(catalog.getBooks().stream()
                    .filter(book -> book.getAuthor().toLowerCase().contains(searchQuery))
                    .map(book -> book.getAuthor() + " - " + book.getTitle())
                    .collect(Collectors.joining(", ")));
        } else {
            view.displayPropertiesMessage("invalidChoice");
        }
    }

    /**
     * Prompts the user for a reader name and a book title, marks the selected book as borrowed by the selected reader,
     * saves the library state, and displays a message indicating that the book has been borrowed.
     * If the reader or book is not found, throws an IllegalArgumentException.
     */
    private void markBookAsBorrowed() {
        String inputName = view.promptForReaderName();
        Reader reader = readers.stream()
                    .filter(r -> r.getName().equalsIgnoreCase(inputName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No such reader found."));

        List<Book> availableBooks = catalog.getBooks().stream()
                    .filter(Book::isAvailable)
                    .collect(Collectors.toList());

        for (int i = 0; i < availableBooks.size(); i++) {
            view.displayMessage((i + 1) + ". " + availableBooks.get(i).getTitle());
        }

        int bookIndex = -1;
        while (bookIndex < 1 || bookIndex > availableBooks.size()) {
            String input = view.promptForBookIndex();
            try {
                bookIndex = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                view.displayMessage("Invalid input. Please enter a number.");
            }
            if (bookIndex < 1 || bookIndex > availableBooks.size()) {
                view.displayMessage("Invalid index. Please enter a number between 1 and " + availableBooks.size() + ".");
            }
        }

        Book book = availableBooks.get(bookIndex - 1);
        reader.borrowBook(book);
        saveLibraryState();
        view.displayPropertiesMessage("borrowedBook");
    }

    /**
     * Allows a reader to return a borrowed book. Prompts the user for the reader's name and displays a list of borrowed books to choose from.
     * Once the user selects a book, it is marked as returned and the library state is saved.
     * If the reader or book is not found, an IllegalArgumentException is thrown.
     */
    private void markBookAsReturned() {
        String inputName = view.promptForReaderName();
        Reader reader = readers.stream()
                    .filter(r -> r.getName().equalsIgnoreCase(inputName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No such reader found."));

        List<Book> borrowedBooks = reader.getBorrowedBooks();
    
        for (int i = 0; i < borrowedBooks.size(); i++) {
            view.displayMessage((i + 1) + ". " + borrowedBooks.get(i).getTitle());
        }
    
        int bookIndex = -1;
        while (bookIndex < 1 || bookIndex > borrowedBooks.size()) {
            String input = view.promptForBookIndex();
            try {
                bookIndex = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                view.displayMessage("Invalid input. Please enter a number.");
            }
            if (bookIndex < 1 || bookIndex > borrowedBooks.size()) {
                view.displayMessage("Invalid index. Please enter a number between 1 and " + borrowedBooks.size() + ".");
            }
        }
    
        Book book = borrowedBooks.get(bookIndex - 1);
        reader.returnBook(book);
        saveLibraryState();
        view.displayPropertiesMessage("returnedBook");
    }

    /**
     * Prompts the user to select a new language and updates the language in the properties file.
     * If the user selects an invalid option, an error message is displayed and the method returns.
     * If the language update fails, an error message is displayed and the method returns.
     * Otherwise, the locale is updated and a success message is displayed.
     */
    private void changeLanguage() {
        String newLanguage = view.promptForLanguageChange();

        if (newLanguage.equals("1")) {
            newLanguage = "en";
        } else if (newLanguage.equals("2")) {
            newLanguage = "bg";
        } else {
            view.displayPropertiesMessage("invalidChoice");
            return;
        }

        try {
            updateLanguageInProperties(newLanguage);
        } catch (IOException ex) {
            view.displayMessage("Failed to change language");
            return;
        }

        view.setLocaleFromProperties();
        view.displayPropertiesMessage("languageChanged");
    }

    /**
     * Updates the language property in the configuration file with the given new language.
     * 
     * @param newLanguage the new language to be set in the configuration file
     * @throws IOException if there is an error while reading or writing the configuration file
     */
    private void updateLanguageInProperties(String newLanguage) throws IOException {
        Properties properties = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(CONFIG_FILENAME))) {
            properties.load(reader);
            properties.setProperty("language", newLanguage);
            properties.store(new FileOutputStream(CONFIG_FILENAME), null);
        }
    }

    /**
     * Sets the exit flag to true, indicating that the program should exit.
     */
    private void exit() {
        exit = true;
    }
}