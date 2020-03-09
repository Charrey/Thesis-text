package exceptions;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.nio.file.Path;

/**
 * The type Parse exception.
 */
public class ParseException extends Throwable {

    private String message;

    /**
     * Instantiates a new Parse exception.
     *
     * @param file the file
     * @param e    the e
     */
    public ParseException(Path file, SAXException e) {
        super(constructMessage(file, e));
    }

    /**
     * Instantiates a new Parse exception.
     *
     * @param file the file
     * @param s    the s
     */
    public ParseException(Path file, String s) {
        super(constructGeneralMessage(file, s));
    }

    private static String constructMessage(Path file, SAXException e) {
        if (e instanceof SAXParseException) {
            return constructLocalizedMessage(file, e);
        } else {
            return constructGeneralMessage(file, e.getMessage());
        }
    }

    private static String constructGeneralMessage(Path file, String e) {
        return e + "\n at " + file;
    }

    private static String constructLocalizedMessage(Path file, SAXException e) {
        return  "[" + ((SAXParseException) e).getColumnNumber() + ":" + ((SAXParseException) e).getLineNumber() + "] " + e.getMessage() + "\n at " + file;
    }
}
