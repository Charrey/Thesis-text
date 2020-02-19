package reader;

import data.graph.HierarchyGraph;
import org.junit.Test;
import org.xml.sax.SAXException;
import test.MyTestCase;

import javax.xml.parsers.ParserConfigurationException;
import main.Reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class TestSimpleMux extends MyTestCase {

    @Test
    public void testSimpleMux() throws IOException, SAXException, ParserConfigurationException {
        Reader reader = new Reader();
        Path mainFile =  resource("../graphml/simpleMUX/main.graphml");
        HierarchyGraph graph = reader.read(mainFile);
        graph.shuffleIdentifiers(100);
        System.out.println(graph.flatten().toDOT());
    }
}
