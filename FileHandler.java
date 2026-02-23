import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileHandler {
    public static void createCatalogIfMissing(Path catalogFile) throws IOException {
        Path catalogDirectory = catalogFile.getParent();
        if (catalogDirectory != null) {
            Files.createDirectories(catalogDirectory);
        }
        if (!Files.exists(catalogFile)) {
            Files.createFile(catalogFile);
        }
    }
    public static LoadResult readCatalogBooks(Path catalogFile) {
    List<Book> booksFromFile = new ArrayList<>();
    int validRecords = 0;
    int invalidRecords = 0;
    List<String> fileLines;
    try {
        fileLines = Files.readAllLines(catalogFile);
    } catch (IOException ioException) {
        ErrorHandler.logFileLineError(catalogFile, "Failed to read catalog file", ioException);
        return new LoadResult(booksFromFile, 0, 1);
    }
    for (String line : fileLines) {
        if (line == null || line.trim().isEmpty()) continue;
        try {
            booksFromFile.add(parseLineToBook(line));
            validRecords++;
        } catch (BookCatalogException exception) {
            ErrorHandler.logFileLineError(catalogFile, line, exception);
            invalidRecords++;
        }
    }
    return new LoadResult(booksFromFile, validRecords, invalidRecords);
}
    public static boolean isIsbnSearch(String operationArgument) {
    if (operationArgument == null || operationArgument.length() != 13)
        return false;
    for (char c : operationArgument.toCharArray()) {
        if (!Character.isDigit(c))
            return false;
    }
    return true;
}
   public static boolean isAddRecordFormat(String operationArgument) {
    return operationArgument != null 
           && operationArgument.split(":").length == 4;
}
    public static List<Book> findByTitleKeyword(List<Book> booksFromFile, String titleKeyword) {
    List<Book> results = new ArrayList<>();
    if (titleKeyword == null) return results;
    for (Book book : booksFromFile) {
        if (book.getTitle().toLowerCase().contains(titleKeyword.toLowerCase())) {
            results.add(book);
        }
    }
    return results;
}
public static Book findByIsbn(List<Book> booksFromFile, String isbnToSearch) throws DuplicateISBNException {
    Book matchedBook = null;
    int isbnMatchCounter = 0;
    for (Book book : booksFromFile) {
        if (!book.getIsbn().equals(isbnToSearch))
            continue;
        isbnMatchCounter++;
        if (isbnMatchCounter > 1)
            throw new DuplicateISBNException("Duplicate ISBN found in catalog: " + isbnToSearch);
        matchedBook = book;
    }
    return matchedBook; 
}
    public static Book parseAddRecord(String addRecordInput) throws BookCatalogException {
        return parseLineToBook(addRecordInput);
    }
    
    public static void appendBookThenSortAndRewrite(Path catalogFile,List<Book> booksFromFile,Book newBook) throws IOException {
    booksFromFile.add(newBook);
    Collections.sort(booksFromFile,
    Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
    List<String> updatedFileLines = new ArrayList<>();
    for (Book book : booksFromFile)
    updatedFileLines.add(book.toCatalogLine());
    Files.write(catalogFile,updatedFileLines,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.CREATE);
}
    static Book parseLineToBook(String catalogLine) throws BookCatalogException {
    String [] fields = catalogLine.split(":");
    if (fields.length != 4) throw new MalformedBookEntryException("Invalid format");
    String title = fields[0].trim();
    String author = fields[1].trim();
    String isbn = fields[2].trim();
    String copiesText = fields[3].trim();
    
    if (title.isEmpty() || author.isEmpty())
        throw new MalformedBookEntryException("Title or author is empty");
    if (isbn.length() != 13)
        throw new InvalidISBNException("ISBN must contain exactly 13 characters");
    for (char ch : isbn.toCharArray()) {
        if (!Character.isDigit(ch))
            throw new InvalidISBNException("ISBN must contain digits only");
    }
   
    int copies;
    try {
        copies = Integer.parseInt(copiesText);
    } catch (NumberFormatException e) {
        throw new MalformedBookEntryException("Copies must be a number");
    }
    if (copies <= 0)
        throw new MalformedBookEntryException("Copies must be positive");

    return new Book(title, author, isbn, copies);
}
}