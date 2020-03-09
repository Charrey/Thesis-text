package charrey.exceptions;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.nio.file.Path;

/**
 * Thrown when a file was read that does not fulfil the format for a hierarchygraph.
 */
public class ParseException extends Throwable {

    /**
     * Instantiates a new ParseException from an exception of the XML parser.
     * @param file File in which the exception takes place
     * @param e    Exception thrown by the XML parser
     */
    public ParseException(Path file, SAXException e) {
        super(constructMessage(file, e));
    }

    /**
     * Instantiates a new Parse exception with a custom message.
     * @param file File in which the exception takes place
     * @param s    Message to display
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
