import java.nio.file.Path;
import java.util.List;

public class LibraryBookTracker {
    public static void main(String[] args) {
        int validRecordsProcessed = 0;
        int searchResultsFound = 0;
        int booksAdded = 0;
        int errorsEncountered = 0;

        Path catalogFile = null;
        String operationArgument = null;

        try {
            if (args.length < 2) {
                InsufficientArgumentsException ex =
                        new InsufficientArgumentsException(
                                "Usage: java LibraryBookTracker <catalogFile.txt> <operationArgument>"
                        );
                ErrorHandler.logUserInputError(Path.of("."), "NO ARGUMENTS", ex);
                throw ex;
            }
            String catalogFileName = args[0];
            operationArgument = args[1];
            if (!catalogFileName.endsWith(".txt")) {
                InvalidFileNameException ex =
                        new InvalidFileNameException("First argument must end with .txt");
                catalogFile = Path.of(catalogFileName);
                ErrorHandler.logUserInputError(catalogFile, catalogFileName, ex);
                throw ex;
            }

            catalogFile = Path.of(catalogFileName);
            FileHandler.createCatalogIfMissing(catalogFile);

            LoadResult loadResult = FileHandler.readCatalogBooks(catalogFile);
            List<Book> booksFromFile = loadResult.getBooksFromFile();
            validRecordsProcessed = loadResult.getValidRecords();
            errorsEncountered += loadResult.getInvalidRecords();

            if (FileHandler.isIsbnSearch(operationArgument)) {
                Book foundBook = FileHandler.findByIsbn(booksFromFile, operationArgument);
                if (foundBook == null) {
                    System.out.println("No matching books found.");
                    searchResultsFound = 0;
                } else {
                    printBooks(List.of(foundBook));
                    searchResultsFound = 1;
                }
            } else if (FileHandler.isAddRecordFormat(operationArgument)) {
                try {
                    Book newBook = FileHandler.parseAddRecord(operationArgument);
                    FileHandler.appendBookThenSortAndRewrite(catalogFile, booksFromFile, newBook);
                    printBooks(List.of(newBook));
                    booksAdded = 1;
                    searchResultsFound = 0;
                } catch (BookCatalogException ex) {
                    errorsEncountered++;
                    System.out.println("Error: " + ex.getMessage());
                    ErrorHandler.logUserInputError(catalogFile, operationArgument, ex);
                }
            } else {
                
                if (operationArgument.contains(":")) {
                    MalformedBookEntryException ex =
                            new MalformedBookEntryException(
                                    "New book record must be: title:author:isbn:copies"
                            );
                    System.out.println("Error: " + ex.getMessage());
                    errorsEncountered++;
                    if (catalogFile != null)
                        ErrorHandler.logUserInputError(catalogFile, operationArgument, ex);
                } else {
                    List<Book> results = FileHandler.findByTitleKeyword(booksFromFile, operationArgument);
                    if (results.isEmpty()) {
                        System.out.println("No matching books found.");
                        searchResultsFound = 0;
                    } else {
                        printBooks(results);
                        searchResultsFound = results.size();
                    }
                }
                booksAdded = 0;
            }

        } catch (BookCatalogException ex) {
            System.out.println("Error: " + ex.getMessage());
            errorsEncountered++;
            if (catalogFile != null)
                ErrorHandler.logUserInputError(catalogFile,
                        (operationArgument == null ? "N/A" : operationArgument),
                        ex);
        } catch (Exception ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
            errorsEncountered++;
            if (catalogFile != null)
                ErrorHandler.logUserInputError(catalogFile,
                        (operationArgument == null ? "N/A" : operationArgument),
                        ex);
        } finally {
            System.out.println();
            System.out.println("----- Statistics -----");
            System.out.println("Valid records processed: " + validRecordsProcessed);
            System.out.println("Search results found: " + searchResultsFound);
            System.out.println("Books added: " + booksAdded);
            System.out.println("Errors encountered: " + errorsEncountered);
            System.out.println("----------------------");
            System.out.println("Thank you for using the Library Book Tracker.");
        }
    }

    private static void printHeader() {
        System.out.printf("%-30s %-20s %-15s %5s%n", "Title", "Author", "ISBN", "Copies");
        System.out.println("--------------------------------------------------------------------------");
    }

    private static void printBookRow(Book book) {
        System.out.printf("%-30s %-20s %-15s %5d%n",
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCopies());
    }

    private static void printBooks(List<Book> books) {
        printHeader();
        for (Book book : books) {
            printBookRow(book);
        }
    }
}