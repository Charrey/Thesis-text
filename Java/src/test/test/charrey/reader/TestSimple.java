package charrey.reader;

import charrey.data.PartialMapping;
import charrey.graph.HierarchyGraph;
import charrey.exceptions.NoMappingException;
import charrey.iso.IsoFinder;
import org.junit.BeforeClass;
import org.junit.Test;
import test.MyTestCase;
import charrey.graph.generator.FPGAGenerator;
import charrey.util.Util;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class TestSimple extends MyTestCase {

    @BeforeClass
    public static void init() {
        System.out.println();
    }

    @Test
    public void testLUT_test1() throws IOException, NoMappingException {
       HierarchyGraph virtual =  FPGAGenerator.makeSimpleLut(2, false);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = virtual.deepCopy().getGraph();
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testRegister_test2() throws IOException, NoMappingException {
        HierarchyGraph virtual = FPGAGenerator.makeSimpleRegister(2, false,false,false,false, false);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = virtual.deepCopy().getGraph();
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testMux_test3() throws IOException, NoMappingException {
        HierarchyGraph virtual = FPGAGenerator.makeSimpleMux(2, false);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = virtual.deepCopy().getGraph();
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testLogicCell_test4() throws IOException, NoMappingException {
        HierarchyGraph virtual = FPGAGenerator.makeSimpleLogicCell(2, false);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = virtual.deepCopy().getGraph();
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testSubdivision_test6() throws IOException, NoMappingException {
        HierarchyGraph concrete = FPGAGenerator.makeSnake(20, 20, false);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAGenerator.makeSnake(2, 3, false);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testLUTMUX_test7() throws IOException, NoMappingException {
        HierarchyGraph concrete = FPGAGenerator.makeSimpleLut(5, false);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAGenerator.makeSimpleMux(2, false);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testMUXLUT_test8() throws IOException, NoMappingException {
        HierarchyGraph concrete = FPGAGenerator.makeLutTree(3, 6, false);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAGenerator.makeSimpleLut(4, false);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testRegisterRegister_test9() throws IOException, NoMappingException {
        HierarchyGraph virtual = FPGAGenerator.makeSimpleRegister(2, true,true,true,true,false);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = FPGAGenerator.makeRegisterEmulator(20);
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testHierarchy_test10() throws NoMappingException, IOException {
        HierarchyGraph virtual = FPGAGenerator.makeRectangleCLBFPGA(1, 2, 2, 2, false);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = FPGAGenerator.makeRectangleCLBFPGA(1, 2, 2, 2, false);
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testHierarchy_test11() throws NoMappingException, IOException {
        HierarchyGraph concrete = FPGAGenerator.makeRectangleCLBFPGA(1, 10, 2, 2, false);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAGenerator.makeRectangleCLBFPGA(2, 2, 2, 2, false);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testHierarchy_test12() throws NoMappingException, IOException {
        HierarchyGraph concrete = FPGAGenerator.makeRectangleCLBFPGA(1, 1, 7, 7, false);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAGenerator.makeRectangleCLBFPGA(1, 2, 2, 2, false);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testHierarchy_test13() throws NoMappingException, IOException {
        HierarchyGraph concrete = FPGAGenerator.makeRectangleCLBFPGA(1, 1, 7, 7, false);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAGenerator.makeRectangleCLBFPGA(2, 2, 2, 2, false);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }
}
