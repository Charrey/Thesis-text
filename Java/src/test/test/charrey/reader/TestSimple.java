package charrey.reader;

import charrey.data.PartialMapping;
import charrey.exceptions.NoMappingException;
import charrey.graph.HierarchyGraph;
import charrey.graph.generator.FPGAGenerator;
import charrey.iso.IsoFinder;
import charrey.util.Util;
import charrey.writer.Writer;
import org.junit.BeforeClass;
import org.junit.Test;
import test.MyTestCase;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class TestSimple extends MyTestCase {

    @BeforeClass
    public static void init() {
        System.out.println();
    }

    @Test
    public void testLUT_test1() throws IOException, NoMappingException {
       HierarchyGraph virtual =  FPGAGenerator.makeSimpleLut(2, true);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = virtual.deepCopy().getGraph();
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testRegister_test2() throws IOException, NoMappingException {
        HierarchyGraph virtual = FPGAGenerator.makeSimpleRegister(2, false,false,false,false, true);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = virtual.deepCopy().getGraph();
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testMux_test3() throws IOException, NoMappingException {
        HierarchyGraph virtual = FPGAGenerator.makeSimpleMux(2, true);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = virtual.deepCopy().getGraph();
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testLogicCell_test4() throws IOException, NoMappingException {
        HierarchyGraph virtual = FPGAGenerator.makeSimpleLogicCell(2, true);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = virtual.deepCopy().getGraph();
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testSubdivision_test6() throws IOException, NoMappingException {
        HierarchyGraph concrete = FPGAGenerator.makeSnake(20, 20, true);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAGenerator.makeSnake(2, 3, true);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testLUTMUX_test7() throws IOException, NoMappingException {
        HierarchyGraph concrete = FPGAGenerator.makeSimpleLut(5, true);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAGenerator.makeSimpleMux(2, true);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testMUXLUT_test8() throws IOException, NoMappingException {
        HierarchyGraph concrete = FPGAGenerator.makeLutTree(3, 6, true);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAGenerator.makeSimpleLut(4, true);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testRegisterRegister_test9() throws IOException, NoMappingException {
        HierarchyGraph virtual = FPGAGenerator.makeSimpleRegister(2, true,true,true,true,true);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = FPGAGenerator.makeRegisterEmulator(20, true);
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testHierarchy_test10() throws NoMappingException, IOException {
        HierarchyGraph virtual = FPGAGenerator.makeRectangleCLBFPGA(1, 2, 2, 2, true);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = FPGAGenerator.makeRectangleCLBFPGA(1, 2, 2, 2, true);
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testHierarchy_test11() throws NoMappingException, IOException {
        HierarchyGraph concrete = FPGAGenerator.makeRectangleCLBFPGA(1, 10, 2, 2, true);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAGenerator.makeRectangleCLBFPGA(2, 2, 2, 2, true);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testHierarchy_test12() throws NoMappingException, IOException {
        HierarchyGraph concrete = FPGAGenerator.makeRectangleCLBFPGA(1, 1, 7, 7, true);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAGenerator.makeRectangleCLBFPGA(1, 2, 2, 2, true);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testHierarchy_test13() throws NoMappingException, IOException {
        HierarchyGraph concrete = FPGAGenerator.makeRectangleCLBFPGA(1, 1, 7, 7, true);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAGenerator.makeRectangleCLBFPGA(2, 2, 2, 2, true);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void hansGrafen() throws IOException {
        HierarchyGraph graph;
        graph = HierarchyGraph.getFlat(FPGAGenerator.makeRectangleCLBFPGA(1, 1, 2, 2, true), 999, false);
        Writer.writeToFile(graph.toDOT(false), Paths.get("C:\\Users\\Pim van Leeuwen\\Desktop\\University\\Afstuderen\\Project\\Local Tex\\Java\\hans\\graph1.dot"));

    }
}
