package reader;

import data.MappingFunction;
import data.graph.HierarchyGraph;
import exceptions.NoMappingException;
import iso.IsoFinder;
import org.junit.Test;
import test.MyTestCase;
import testMaker.FPGAModels;
import util.Util;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class TestSimple extends MyTestCase {

    @Test
    public void testLUT_test1() throws IOException, NoMappingException {
       HierarchyGraph graph1 =  FPGAModels.makeSimpleLut(2, false);
        graph1.shuffleIdentifiers();
        HierarchyGraph graph2 = graph1.deepCopy().getGraph();
        graph2.shuffleIdentifiers();
        MappingFunction f = IsoFinder.getMapping(graph1, graph2);
        assertTrue(Util.isCorrect(f.getPartialMapping()));
    }

    @Test
    public void testRegister_test2() throws IOException, NoMappingException {
        long base = System.currentTimeMillis();
        HierarchyGraph graph1 = FPGAModels.makeSimpleRegister(2, false);
        graph1.shuffleIdentifiers();
        HierarchyGraph graph2 = graph1.deepCopy().getGraph();
        graph2.shuffleIdentifiers();
        MappingFunction f = IsoFinder.getMapping(graph1, graph2);
        assertTrue(Util.isCorrect(f.getPartialMapping()));
    }

    @Test
    public void testMux_test3() throws IOException, NoMappingException {
        HierarchyGraph graph1 = FPGAModels.makeSimpleMux(2, false);
        graph1.shuffleIdentifiers();
        HierarchyGraph graph2 = graph1.deepCopy().getGraph();
        graph2.shuffleIdentifiers();
        MappingFunction f = IsoFinder.getMapping(graph1, graph2);
        assertTrue(Util.isCorrect(f.getPartialMapping()));
    }

    @Test
    public void testLogicCell_test4() throws IOException, NoMappingException {
        HierarchyGraph graph1 = FPGAModels.makeSimpleLogicCell(2, false);
        graph1.shuffleIdentifiers();
        HierarchyGraph graph2 = graph1.deepCopy().getGraph();
        graph2.shuffleIdentifiers();
        MappingFunction f = IsoFinder.getMapping(graph1, graph2);
        assertTrue(Util.isCorrect(f.getPartialMapping()));
    }

    @Test
    public void testSubdivision_test5() throws IOException, NoMappingException {
        HierarchyGraph graph1 = FPGAModels.makeSnake(100, 100, false);
        graph1.shuffleIdentifiers();
        HierarchyGraph graph2 = FPGAModels.makeSnake(2, 3, false);
        graph2.shuffleIdentifiers();
        MappingFunction f = IsoFinder.getMapping(graph1, graph2);
        assertTrue(Util.isCorrect(f.getPartialMapping()));
    }

    @Test
    public void testLUTMUX_test6() throws IOException, NoMappingException {
        HierarchyGraph graph1 = FPGAModels.makeSimpleLut(5, false);
        graph1.shuffleIdentifiers();
        HierarchyGraph graph2 = FPGAModels.makeSimpleMux(2, false);
        graph2.shuffleIdentifiers();
        MappingFunction f = IsoFinder.getMapping(graph2, graph1);
        assertTrue(Util.isCorrect(f.getPartialMapping()));
    }

    @Test
    public void testMUXLUT_test7() throws IOException, NoMappingException {
        HierarchyGraph graph1 = FPGAModels.makeLutTree(3, 6, false);
        graph1.shuffleIdentifiers();
        HierarchyGraph graph2 = FPGAModels.makeSimpleLut(4, false);
        graph2.shuffleIdentifiers();
        MappingFunction f = IsoFinder.getMapping(graph2, graph1);
        assertTrue(Util.isCorrect(f.getPartialMapping()));
    }
}
