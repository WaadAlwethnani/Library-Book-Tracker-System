import java.util.List;
public class LoadResult {

    private final List<Book> booksFromFile;
    private final int validRecords;
    private final int invalidRecords;

    public LoadResult(List<Book> booksFromFile, int validRecords,int invalidRecords) {
        this.booksFromFile = booksFromFile;
        this.validRecords = validRecords;
        this.invalidRecords = invalidRecords;
    }

    public List<Book> getBooksFromFile() {
        return booksFromFile;
    }

    public int getValidRecords() {
        return validRecords;
    }

    public int getInvalidRecords() {
        return invalidRecords;
    }
}