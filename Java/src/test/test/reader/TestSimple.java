package reader;

import data.graph.HierarchyGraph;
import exceptions.ParseException;
import org.eclipse.collections.api.tuple.Pair;
import org.junit.Test;
import test.MyTestCase;
import writer.Writer;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestSimple extends MyTestCase {

    @Test
    public void testSimpleMux() throws IOException, ParserConfigurationException, ParseException {
        Reader reader = new Reader();
        Path mainFile =  resource("../graphml/manual/simpleMUX/main.graphml");
        HierarchyGraph graph = reader.read(mainFile);
        graph.shuffleIdentifiers();
        System.out.println(graph.flatten().toDOT(true));
    }

    @Test
    public void testMuxExport() throws IOException, ParserConfigurationException, ParseException {
        Path mainFile =  resource("../graphml/manual/simpleMUX/main.graphml");
        String contents = new String(Files.readAllBytes(mainFile), StandardCharsets.UTF_8);
        HierarchyGraph graph = new Reader().read(contents, mainFile);
        Writer.writeToDirectory(Writer.export(graph, true), Paths.get("export/MuxExport"));

        contents = new String(Files.readAllBytes(Paths.get("export/MuxExport").resolve("main.graphml")), StandardCharsets.UTF_8);
        graph = new Reader().read(contents, (Paths.get("export/MuxExport").resolve("main.graphml")));
        System.out.println(graph.flatten().toDOT(false));
    }

    @Test
    public void testSimpleLut() throws IOException, ParserConfigurationException, ParseException {
        Reader reader = new Reader();
        Path mainFile =  resource("../graphml/manual/simpleLUT/main.graphml");
        HierarchyGraph graph = reader.read(mainFile);
        graph.shuffleIdentifiers();
        System.out.println(graph.flatten().toDOT(true));
    }

    @Test
    public void testSimpleRegister() throws IOException, ParserConfigurationException, ParseException {
        Reader reader = new Reader();
        Path mainFile =  resource("../graphml/manual/simpleRegister/main.graphml");
        HierarchyGraph graph = reader.read(mainFile);
        graph.shuffleIdentifiers();
        System.out.println(graph.flatten().toDOT(true));
    }

    @Test
    public void testSimpleSwitch() throws IOException, ParserConfigurationException, ParseException {
        Reader reader = new Reader();
        Path mainFile =  resource("../graphml/manual/simpleSwitch/main.graphml");
        HierarchyGraph graph = reader.read(mainFile);
        graph.shuffleIdentifiers();
        System.out.println(graph.flatten().toDOT(true));
    }

    @Test
    public void testSimpleCell() throws IOException, ParserConfigurationException, ParseException {
        Reader reader = new Reader();
        Path mainFile =  resource("../graphml/manual/simpleSwitch/main.graphml");
        HierarchyGraph graph = reader.read(mainFile);
        graph.shuffleIdentifiers();
        System.out.println(graph.flatten().toDOT(true));
    }
}
