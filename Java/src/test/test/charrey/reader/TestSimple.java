package reader;

import data.PartialMapping;
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
       HierarchyGraph virtual =  FPGAModels.makeSimpleLut(2, false);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = virtual.deepCopy().getGraph();
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testRegister_test2() throws IOException, NoMappingException {
        HierarchyGraph virtual = FPGAModels.makeSimpleRegister(2, false,false,false,false, false);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = virtual.deepCopy().getGraph();
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testMux_test3() throws IOException, NoMappingException {
        HierarchyGraph virtual = FPGAModels.makeSimpleMux(2, false);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = virtual.deepCopy().getGraph();
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testLogicCell_test4() throws IOException, NoMappingException {
        HierarchyGraph virtual = FPGAModels.makeSimpleLogicCell(2, false);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = virtual.deepCopy().getGraph();
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testSubdivision_test6() throws IOException, NoMappingException {
        HierarchyGraph concrete = FPGAModels.makeSnake(20, 20, false);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAModels.makeSnake(2, 3, false);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testLUTMUX_test7() throws IOException, NoMappingException {
        HierarchyGraph concrete = FPGAModels.makeSimpleLut(5, false);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAModels.makeSimpleMux(2, false);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testMUXLUT_test8() throws IOException, NoMappingException {
        HierarchyGraph concrete = FPGAModels.makeLutTree(3, 6, false);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAModels.makeSimpleLut(4, false);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testRegisterRegister_test9() throws IOException, NoMappingException {
        HierarchyGraph virtual = FPGAModels.makeSimpleRegister(2, true,true,true,true,false);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = FPGAModels.makeRegisterEmulator(20);
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testHierarchy_test10() throws NoMappingException, IOException {
        HierarchyGraph virtual = FPGAModels.makeRectangleCLBFPGA(1, 2, 2, 2, false);
        virtual.shuffleIdentifiers();
        HierarchyGraph concrete = FPGAModels.makeRectangleCLBFPGA(1, 2, 2, 2, false);
        concrete.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testHierarchy_test11() throws NoMappingException, IOException {
        HierarchyGraph concrete = FPGAModels.makeRectangleCLBFPGA(1, 10, 2, 2, false);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAModels.makeRectangleCLBFPGA(2, 2, 2, 2, false);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testHierarchy_test12() throws NoMappingException, IOException {
        HierarchyGraph concrete = FPGAModels.makeRectangleCLBFPGA(1, 1, 7, 7, false);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAModels.makeRectangleCLBFPGA(1, 2, 2, 2, false);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }

    @Test
    public void testHierarchy_test13() throws NoMappingException, IOException {
        HierarchyGraph concrete = FPGAModels.makeRectangleCLBFPGA(1, 1, 7, 7, false);
        concrete.shuffleIdentifiers();
        HierarchyGraph virtual = FPGAModels.makeRectangleCLBFPGA(2, 2, 2, 2, false);
        virtual.shuffleIdentifiers();
        PartialMapping f = IsoFinder.getMapping(virtual, concrete);
        assertTrue(Util.isCorrect(f));
    }
}
