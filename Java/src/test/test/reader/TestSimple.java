package reader;

import data.MappingFunction;
import data.graph.HierarchyGraph;
import exceptions.NoMappingException;
import exceptions.ParseException;
import iso.IsoFinder;
import org.eclipse.collections.api.tuple.Pair;
import org.junit.Test;
import test.MyTestCase;
import testMaker.MakeTests;
import util.Util;
import writer.Writer;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSimple extends MyTestCase {

    @Test
    public void testLUT_test1() throws IOException, ParserConfigurationException, ParseException, NoMappingException {
        MakeTests.makeSimpleLut(2);
        Path mainFile =  resource("../graphml/LUT/main.graphml");
        HierarchyGraph graph1 = new Reader().read(mainFile);
        graph1.shuffleIdentifiers();
        HierarchyGraph graph2 = graph1.deepCopy().getOne();
        graph2.shuffleIdentifiers();
        MappingFunction f = IsoFinder.getMapping(graph1, graph2);
        assertTrue(Util.isCorrect(f.getPartialMapping()));
    }

    @Test
    public void testRegister_test2() throws IOException, ParserConfigurationException, ParseException, NoMappingException {
        MakeTests.makeSimpleRegister(2);
        Path mainFile =  resource("../graphml/Register/main.graphml");
        HierarchyGraph graph1 = new Reader().read(mainFile);
        graph1.shuffleIdentifiers();
        HierarchyGraph graph2 = graph1.deepCopy().getOne();
        graph2.shuffleIdentifiers();
        MappingFunction f = IsoFinder.getMapping(graph1, graph2);
        assertTrue(Util.isCorrect(f.getPartialMapping()));
    }

    @Test
    public void testMux_test3() throws IOException, ParserConfigurationException, ParseException, NoMappingException {
        MakeTests.makeSimpleMux(2);
        Path mainFile =  resource("../graphml/MUX/main.graphml");
        HierarchyGraph graph1 = new Reader().read(mainFile);
        graph1.shuffleIdentifiers();
        HierarchyGraph graph2 = graph1.deepCopy().getOne();
        graph2.shuffleIdentifiers();
        MappingFunction f = IsoFinder.getMapping(graph1, graph2);
        assertTrue(Util.isCorrect(f.getPartialMapping()));
    }

    @Test
    public void testLogicCell_test4() throws IOException, ParserConfigurationException, ParseException, NoMappingException {
        MakeTests.makeSimpleLogicCell(2);
        Path mainFile =  resource("../graphml/LogicCell/main.graphml");
        HierarchyGraph graph1 = new Reader().read(mainFile);
        graph1.shuffleIdentifiers();
        HierarchyGraph graph2 = graph1.deepCopy().getOne();
        graph2.shuffleIdentifiers();
        MappingFunction f = IsoFinder.getMapping(graph1, graph2);
        assertTrue(Util.isCorrect(f.getPartialMapping()));
    }


}
