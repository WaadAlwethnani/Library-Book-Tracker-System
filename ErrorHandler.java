import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class ErrorHandler {

    public static void logFileLineError(Path catalogFile, String faultyLine, Exception thrownException) {
        writeErrorEntry(catalogFile, "INVALID LINE", faultyLine, thrownException);
    }

    public static void logUserInputError(Path catalogFile, String invalidInput, Exception thrownException) {
        writeErrorEntry(catalogFile, "INVALID INPUT", invalidInput, thrownException);
    }

    private static void writeErrorEntry(Path catalogFile, String errorCategory, String relatedText,Exception thrownException) {
        try {
            Path parentDirectory = catalogFile.getParent();
            if (parentDirectory == null) {
                parentDirectory = Path.of(".");
            }
            Path logFilePath = parentDirectory.resolve("errors.log");
            String logEntry =
                    "[" + LocalDateTime.now().withNano(0) + "] "
                            + errorCategory + ": "
                            + "\"" + relatedText + "\""
                            + System.lineSeparator()
                            + thrownException.getClass().getSimpleName()
                            + ": "
                            + thrownException.getMessage()
                            + System.lineSeparator();
            Files.write(logFilePath,
                    logEntry.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ioFailure) {
            System.out.println("Unable to write to errors.log");
        }
    }
}