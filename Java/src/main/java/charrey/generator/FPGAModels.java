package charrey.testMaker;

import charrey.data.graph.HierarchyGraph;
import charrey.data.graph.Label;
import charrey.data.graph.Patterns;
import charrey.data.graph.Vertex;
import charrey.data.patterns.*;
import charrey.util.BiMap;
import charrey.util.Util;
import charrey.writer.Writer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The type Fpga models.
 */
public class FPGAModels {

    /**
     * Make lut and register hierarchy graph.
     *
     * @param ins   the ins
     * @param write the write
     * @return the hierarchy graph
     * @throws IOException the io exception
     */
    public static HierarchyGraph makeLutAndRegister(int ins, boolean write) throws IOException {
        HierarchyGraph res = new HierarchyGraph();
        Register register = Patterns.Register(ins, true, false, false, false, false);
        List<Vertex> pins = new LinkedList<>();
        List<Vertex> ports = new LinkedList<>();
        Vertex clock = res.addVertex(Label.CLOCK_FRAME);
        Vertex component = res.addComponent(register.hierarchyGraph, "Register");
        res.addEdge(clock, res.addPort(register.syncSet, component));
        for (int i = 0; i < ins; i++) {
            pins.add(res.addVertex(Label.PIN));
            ports.add(res.addPort(register.inputs.get(i), component));
        }
        for (int i = 0; i < ins; i++) {
            pins.add(res.addVertex(Label.PIN));
            ports.add(res.addPort(register.outputs.get(i), component));
        }
        for (int i = 0; i < pins.size(); i++) {
            res.addEdge(pins.get(i), ports.get(i));
        }

        LUT lut = Patterns.LUT(ins + 1, ins);
        pins = new LinkedList<>();
        ports = new LinkedList<>();
        component = res.addComponent(lut.getHierarchyGraph(), "LUT");
        res.addEdge(clock, res.addPort(lut.getInputs().get(2), component));
        for (int i = 0; i < ins; i++) {
            pins.add(res.addVertex(Label.PIN));
            ports.add(res.addPort(lut.getInputs().get(i), component));
        }
        for (int i = 0; i < ins; i++) {
            pins.add(res.addVertex(Label.PIN));
            ports.add(res.addPort(lut.getOutputs().get(i), component));
        }
        for (int i = 0; i < pins.size(); i++) {
            res.addEdge(pins.get(i), ports.get(i));
        }
        if (write) {
            Writer.export(res, true, Paths.get("src/test/resources/graphml/LutAndRegister"));
        }
        return res;
    }

    /**
     * Make simple routing switch hierarchy graph.
     *
     * @param write the write
     * @return the hierarchy graph
     * @throws IOException the io exception
     */
    public static HierarchyGraph makeSimpleRoutingSwitch(boolean write) throws IOException {
        Switch mySwitch = Patterns.Switch();
        mySwitch.addOption(new Patterns.IntConnection(0, 1), new Patterns.IntConnection(1, 0));
        mySwitch.addOption(new Patterns.IntConnection(0, 2), new Patterns.IntConnection(2, 0));
        HierarchyGraph res = new HierarchyGraph();
        Vertex component = res.addComponent(mySwitch.getHierarchyGraph(), "switch");
        List<Vertex> pins = Patterns.addPins(res, 3);
        List<Vertex> ports = new LinkedList<>();//Patterns.addPorts(res, 3);
        for (int i = 0; i < pins.size(); i++) {
            ports.add(res.addPort(mySwitch.getByNumber(i), component));
            res.addEdge(ports.get(i), component);
            res.addEdge(pins.get(i), ports.get(i));
        }
        if (write) {
            Writer.export(res, true, Paths.get("src/test/resources/graphml/Switch"));
        }
        return res;
    }

    /**
     * Make simple mux hierarchy graph.
     *
     * @param wireCount the wire count
     * @param write     the write
     * @return the hierarchy graph
     * @throws IOException the io exception
     */
    public static HierarchyGraph makeSimpleMux(int wireCount, boolean write) throws IOException {
        MUX mux = Patterns.MUX(wireCount);
        HierarchyGraph res = new HierarchyGraph();
        Vertex component = res.addComponent(mux.hierarchyGraph, "MUX");
        List<Vertex> pins = Patterns.addPins(res, 3*wireCount + 1);
        List<Vertex> ports = new LinkedList<>();//Patterns.addPorts(res, 3*wireCount + 1);
        for (int i = 0; i < pins.size(); i++) {
            ports.add(res.addPort(Util.concat(mux.in1, mux.in2, mux.out, List.of(mux.select)).get(i), component));
        }
        for (int i = 0; i < pins.size(); i++) {
            res.addEdge(pins.get(i), ports.get(i));
        }
        if (write) {
            Writer.export(res, true, Paths.get("src/test/resources/graphml/MUX"));
        }
        return res;
    }

    /**
     * Make simple logic cell hierarchy graph.
     *
     * @param wires the wires
     * @param write the write
     * @return the hierarchy graph
     * @throws IOException the io exception
     */
    public static HierarchyGraph makeSimpleLogicCell(int wires, boolean write) throws IOException {
        HierarchyGraph graph = new HierarchyGraph();

        List<Vertex> pins = Patterns.addPins(graph, wires * 2);
        Vertex clock1 = graph.addVertex(Label.CLOCK_FRAME);
        graph.addVertex(Label.CLOCK_FRAME);
        LogicCell logicCell = Patterns.LogicCell(wires);
        Vertex component = graph.addComponent(logicCell.graph, "Logic Cell");

        for (int i = 0; i < wires; i++) {
            graph.addEdge(pins.get(i), graph.addPort(logicCell.inputs.get(i), component));
            graph.addEdge(pins.get(i + wires), graph.addPort(logicCell.outputs.get(i), component));
        }
        graph.addEdge(clock1, graph.addPort(logicCell.clockPort, component));
        if (write) {
            Writer.export(graph, true, Paths.get("src/test/resources/graphml/LogicCell"));
        }
        return graph;
    }

    /**
     * Make simple register hierarchy graph.
     *
     * @param wireCount   the wire count
     * @param asyncSet    the async set
     * @param syncReset   the sync reset
     * @param asyncReset  the async reset
     * @param clockEnable the clock enable
     * @param write       the write
     * @return the hierarchy graph
     * @throws IOException the io exception
     */
    public static HierarchyGraph makeSimpleRegister(int wireCount, boolean asyncSet, boolean syncReset, boolean asyncReset, boolean clockEnable, boolean write) throws IOException {
        Register register = Patterns.Register(wireCount, true, asyncSet, syncReset, asyncReset, clockEnable);
        List<Vertex> lowerPorts = Util.concat(register.inputs, register.outputs, Util.listOf(register.asyncSet), Util.listOf(register.syncReset), Util.listOf(register.asyncReset), Util.listOf(register.clockEnable));
        HierarchyGraph res = new HierarchyGraph();

        Vertex component = res.addComponent(register.hierarchyGraph, "Register");
        List<Vertex> pins = Patterns.addPins(res, lowerPorts.size());
        for (int i = 0; i < lowerPorts.size(); i++) {
            res.addEdge(pins.get(i), res.addPort(lowerPorts.get(i), component));
        }
        res.addEdge(res.addVertex(Label.CLOCK_FRAME), res.addPort(register.syncSet, component));
        res.addVertex(Label.CLOCK_FRAME);
        if (write) {
            Writer.export(res, true, Paths.get("src/test/resources/graphml/Register"));
        }
        return res;
    }

    /**
     * Make simple lut hierarchy graph.
     *
     * @param ins   the ins
     * @param write the write
     * @return the hierarchy graph
     * @throws IOException the io exception
     */
    public static HierarchyGraph makeSimpleLut(int ins, boolean write) throws IOException {
        LUT lut = Patterns.LUT(ins, ins);
        HierarchyGraph res = new HierarchyGraph();

        Vertex component = res.addComponent(lut.getHierarchyGraph(), "LUT");
        List<Vertex> pins = Patterns.addPins(res, ins*2);
        List<Vertex> ports = new LinkedList<>();//Patterns.addPorts(res, ins*2);
        for (int i = 0; i < pins.size(); i++) {
            ports.add(res.addPort(Util.concat(lut.getInputs(), lut.getOutputs()).get(i), component));
            res.addEdge(ports.get(i), component);
        }
        for (int i = 0; i < ports.size(); i++) {
            res.addEdge(ports.get(i), pins.get(i));
        }
        if (write) {
            Writer.export(res, true, Paths.get("src/test/resources/graphml/LUT"));
        }
        return res;
    }

    /**
     * Make snake hierarchy graph.
     *
     * @param width  the width
     * @param length the length
     * @param write  the write
     * @return the hierarchy graph
     * @throws IOException the io exception
     */
    public static HierarchyGraph makeSnake(int width, int length, boolean write) throws IOException {
        assert width > 0;
        HierarchyGraph graph = new HierarchyGraph();
        Vertex[][] components = new Vertex[width][length];

        Vertex[][] leftPorts = new Vertex[width][length];
        Vertex[][] rightPorts = new Vertex[width][length];
        Vertex[][] upPorts = new Vertex[width - 1][length];
        Vertex[][] downPorts = new Vertex[width - 1][length];

        List<Vertex> inputPins = Patterns.addPins(graph, width);
        List<Vertex> outputPins = Patterns.addPins(graph, width);

        Switch edgeSwitches = Patterns.Switch();
        edgeSwitches.addOption(new Patterns.IntConnection(0, 1), new Patterns.IntConnection(1, 0));
        edgeSwitches.addOption(new Patterns.IntConnection(1, 2), new Patterns.IntConnection(2, 1));
        edgeSwitches.addOption(new Patterns.IntConnection(2, 0), new Patterns.IntConnection(0, 2));
        HierarchyGraph edgeSwitchGraph = edgeSwitches.getHierarchyGraph();

        for (int i = 0; i < length; i++) { //Bottom side
            components[width-1][i] = graph.addComponent(edgeSwitchGraph, "edgeSwitch");
            leftPorts[width-1][i] = graph.addPort(edgeSwitches.getByNumber(0), components[width-1][i]);
            rightPorts[width-1][i] = graph.addPort(edgeSwitches.getByNumber(1), components[width-1][i]);
            if (width > 1) {
                upPorts[width-2][i] = graph.addPort(edgeSwitches.getByNumber(2), components[width - 1][i]);
            }
        }
        if (width > 1) {
            for (int i = 0; i < length; i++) { //Top size
                components[0][i] = graph.addComponent(edgeSwitchGraph, "edgeSwitch");
                leftPorts[0][i] = graph.addPort(edgeSwitches.getByNumber(0), components[0][i]);
                rightPorts[0][i] = graph.addPort(edgeSwitches.getByNumber(1), components[0][i]);
                downPorts[0][i] = graph.addPort(edgeSwitches.getByNumber(2), components[0][i]);
            }
        }

        Switch middleSwitches = Patterns.Switch();
        middleSwitches.addOption(new Patterns.IntConnection(0, 1), new Patterns.IntConnection(1, 0));
        middleSwitches.addOption(new Patterns.IntConnection(1, 2), new Patterns.IntConnection(2, 1));
        middleSwitches.addOption(new Patterns.IntConnection(2, 3), new Patterns.IntConnection(3, 2));
        middleSwitches.addOption(new Patterns.IntConnection(3, 0), new Patterns.IntConnection(0, 3));
        HierarchyGraph middleSwitchGraph = middleSwitches.getHierarchyGraph();

        for (int i = 1; i < width - 1; i++) {
            for (int j = 0; j < length; j++) {
                components[i][j] = graph.addComponent(middleSwitchGraph, "middleSwitch");
                leftPorts[i][j] = graph.addPort(middleSwitches.getByNumber(0), components[i][j]);
                rightPorts[i][j] = graph.addPort(middleSwitches.getByNumber(1), components[i][j]);
                upPorts[i-1][j] = graph.addPort(middleSwitches.getByNumber(2), components[i][j]);
                downPorts[i][j] = graph.addPort(middleSwitches.getByNumber(3), components[i][j]);
            }
        }

        //Connect pins
        for (int i = 0; i < width; i++) {
            graph.addEdge(inputPins.get(i), leftPorts[i][0]);
            graph.addEdge(outputPins.get(i), rightPorts[i][length-1]);
        }

        //connect top to bottom
        for (int i = 0; i < width - 1; i++) {
            for (int j = 0; j < length; j++) {
                graph.addEdge(downPorts[i][j], upPorts[i][j]);
            }
        }

        //connect left to right
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length - 1; j++) {
                graph.addEdge(rightPorts[i][j], leftPorts[i][j+1]);
            }
        }
        if (write) {
            Writer.export(graph, true, Paths.get("src/test/resources/graphml/LUT"));
        }
        return graph;
    }

    /**
     * Make lut tree hierarchy graph.
     *
     * @param smallLutWires the small lut wires
     * @param bigLutWires   the big lut wires
     * @param write         the write
     * @return the hierarchy graph
     * @throws IOException the io exception
     */
    public static HierarchyGraph makeLutTree(int smallLutWires, int bigLutWires, boolean write) throws IOException {
        HierarchyGraph res =  HierarchyGraph.getFlat(getLutTree(smallLutWires, bigLutWires).getHierarchyGraph(), 999, false);
        for (Vertex vertex : res.getVerticesByLabel(Label.REMOVE)) {
            if (res.getEdges().get(vertex).size()==1) {
                res.addEdge(vertex, res.addVertex(Label.PIN));
            }
        }
        recursiveRemove(res, x -> x.getLabels().contains(Label.REMOVE));
        return res;

    }

    private static void recursiveRemove(HierarchyGraph res, Predicate<Vertex> predicate) {
        recursiveRemove(res, predicate, new BiMap<>());
    }
    private static void recursiveRemove(HierarchyGraph res, Predicate<Vertex> predicate, BiMap<Vertex, Vertex> dangerous) {
        while (res.getVertices().stream().filter(x -> !dangerous.containsValue(x)).anyMatch(predicate)) {
            Vertex example = res.getVertices().stream().filter(x -> predicate.test(x) && !dangerous.containsValue(x)).findAny().get();
            for (Vertex one : res.getEdges().get(example)) {
                for (Vertex two : res.getEdges().get(example)) {
                    if (two.getID() > one.getID()) {
                        res.addEdge(one, two);
                    }
                }
            }
            res.removeVertex(example);
        }
        for (HierarchyGraph graph : res.getHierarchy().values()) {
            recursiveRemove(graph, predicate, new BiMap<>(res.getPortMapping()));
        }
    }

    private static LUT getLutTree(int smallLutWires, int bigLutWires) throws IOException {
        if (smallLutWires == bigLutWires) {
            return Patterns.LUT(smallLutWires, 4);
        } else {
            HierarchyGraph res = new HierarchyGraph();
            LUT oneSmaller = getLutTree(smallLutWires, bigLutWires - 1);
            MUX mux = Patterns.MUX(smallLutWires);
            Vertex LutComponent1 = res.addComponent(oneSmaller.getHierarchyGraph(), "LowerLut");
            Vertex LutComponent2 = res.addComponent(oneSmaller.getHierarchyGraph(), "LowerLut");
            Vertex MuxComponent = res.addComponent(mux.hierarchyGraph, "MUX");
            List<Vertex> upperLutInPorts = new LinkedList<>();
            List<Vertex> lowerLutInPorts = new LinkedList<>();
            List<Vertex> upperLutOutPorts = new LinkedList<>();
            List<Vertex> lowerLutOutPorts = new LinkedList<>();
            List<Vertex> muxUpperInPorts = new LinkedList<>();
            List<Vertex> muxLowerInPorts = new LinkedList<>();
            List<Vertex> muxOutPorts = new LinkedList<>();

            List<Vertex> lutTreeOutPorts = new LinkedList<>();
            List<Vertex> lutTreeInPorts = new LinkedList<>();
            /*
                   /|---/\---
            ------| |---\/---
                  | |
            ------| |---/\---
                  |\|---\/---
            -----/mux  luttreex2
             */
            //Connect outputs of LUTs to this FPGA
            for (Vertex subNode : oneSmaller.getOutputs()) {
                upperLutOutPorts.add(res.addPort(subNode, LutComponent1));
                lowerLutOutPorts.add(res.addPort(subNode, LutComponent2));
            }
            //Connect inputs of LUTs to this FPGA
            for (Vertex subNode : oneSmaller.getInputs()) {
                upperLutInPorts.add(res.addPort(subNode, LutComponent1));
                lowerLutInPorts.add(res.addPort(subNode, LutComponent2));
            }
            //Connect upper input of mux to FPGA
            for (Vertex subNode : mux.in1) {
                muxUpperInPorts.add(res.addPort(subNode, MuxComponent));
            }
            //Connect lower input of mux to FPGA
            for (Vertex subNode : mux.in2) {
                muxLowerInPorts.add(res.addPort(subNode, MuxComponent));
            }
            //Connect outputs of mux to FPGA
            for (Vertex subNode : mux.out) {
                muxOutPorts.add(res.addPort(subNode, MuxComponent));
            }
            //Connect selector of mux to FPGA
            Vertex selectPort = res.addPort(mux.select, MuxComponent);
            for (int i = 0; i < smallLutWires; i++) {
                res.addEdge(upperLutOutPorts.get(i), muxUpperInPorts.get(i));
                res.addEdge(lowerLutOutPorts.get(i), muxLowerInPorts.get(i));
            }
            for (Vertex inputPort : Util.concat(upperLutInPorts, lowerLutInPorts, List.of(selectPort))) {
                Vertex toAdd = res.addVertex(Label.REMOVE);
                res.addEdge(toAdd, inputPort);
                lutTreeInPorts.add(toAdd);
            }
            for (Vertex outputPort : muxOutPorts) {
                Vertex toAdd = res.addVertex(Label.REMOVE);
                res.addEdge(toAdd, outputPort);
                lutTreeOutPorts.add(toAdd);
            }
            return new LeafLUT(res, lutTreeInPorts, lutTreeOutPorts);
        }
    }

    /**
     * Make register emulator hierarchy graph.
     *
     * @param wireCount the wire count
     * @return the hierarchy graph
     */
    public static HierarchyGraph makeRegisterEmulator(int wireCount) {
        HierarchyGraph graph = new HierarchyGraph();
        Register register = Patterns.Register(wireCount, true, false, false, false, false);
        Vertex Registercomponent = graph.addComponent(register.hierarchyGraph, "register");
        Vertex frame1 = graph.addVertex(Label.CLOCK_FRAME);
        Vertex frame2 = graph.addVertex(Label.CLOCK_FRAME);
        LUT lut = Patterns.LUT(2, 1);
        Vertex LutComponent = graph.addComponent(lut.getHierarchyGraph(), "lut");
        List<Vertex> pins = Patterns.addPins(graph, wireCount *2 + 1);
        graph.addEdge(pins.get(0), graph.addPort(lut.getInputs().get(0), LutComponent));
        graph.addEdge(frame1, graph.addPort(lut.getInputs().get(1), LutComponent));
        graph.addEdge(graph.addPort(lut.getOutputs().get(0), LutComponent), graph.addPort(register.outputs.get(0), Registercomponent));

        for (int i = 0; i < wireCount; i++) {
            graph.addEdge(pins.get(i+1), graph.addPort(register.inputs.get(i), Registercomponent));
            graph.addEdge(pins.get(i+wireCount+1), graph.addPort(register.outputs.get(i), Registercomponent));
        }
        return graph;

    }

    /**
     * Make rectangle clb hierarchy graph.
     *
     * @param logicCellCount the logic cell count
     * @param wireCount      the wire count
     * @param write          the write
     * @return the hierarchy graph
     * @throws IOException the io exception
     */
    public static HierarchyGraph makeRectangleCLB(int logicCellCount, int wireCount, boolean write) throws IOException {
        HierarchyGraph graph = new HierarchyGraph();
        ConfigurableLogicBlock CLB = Patterns.getRectangleCLB(wireCount, logicCellCount);
        Vertex component = graph.addComponent(CLB.graph, "CLB");
        for (Vertex v : CLB.clocks) {
            graph.addEdge(graph.addPort(v, component), graph.addVertex(Label.PIN));
        }
        graph = HierarchyGraph.getFlat(graph, 999, false);
        recursiveRemove(graph, x -> x.getLabels().contains(Label.REMOVE));
        if (write) {
            Writer.export(graph, true, Paths.get("src/test/resources/graphml/RectangleCLB"));
        }
        return graph;

    }

    /**
     * Make rectangle clbfpga hierarchy graph.
     *
     * @param wireCount        the wire count
     * @param logicCellsPerCLB the logic cells per clb
     * @param clbWidth         the clb width
     * @param clbHeight        the clb height
     * @param write            the write
     * @return the hierarchy graph
     * @throws IOException the io exception
     */
    public static HierarchyGraph makeRectangleCLBFPGA(int wireCount, int logicCellsPerCLB, int clbWidth, int clbHeight, boolean write) throws IOException {
        Util.assertOrElse(clbWidth >= 1, "Needs at least 1 column of CLBs.");
        Util.assertOrElse(clbHeight >= 1, "Needs at least 1 row of CLBs.");
        HierarchyGraph graph = new HierarchyGraph();

        ConfigurableLogicBlock clb = Patterns.getRectangleCLB(wireCount, logicCellsPerCLB);
        Switchblock switchblockMiddle = Patterns.getSwitchBlock(wireCount, false);
        Switchblock switchblockEdge = Patterns.getSwitchBlock(wireCount, true);

        Vertex risingEdge = graph.addVertex(Label.CLOCK_FRAME);
        graph.addVertex(Label.CLOCK_FRAME);
        Vertex[][] clbComponents = new Vertex[clbWidth][clbHeight];
        for (int i = 0; i < clbWidth; i++) {
            for (int j = 0; j < clbHeight; j++) {
                clbComponents[i][j] = graph.addComponent(clb.graph, "CLB");
                for (Vertex clock : clb.clocks) {
                    graph.addEdge(risingEdge, graph.addPort(clock, clbComponents[i][j]));//todo:uncomment
                }
            }
        }

        List<List<List<Vertex>>> topConnections = new LinkedList<>(); //For each CLB row, for each CLB, for each vertex
        List<List<List<Vertex>>> bottomConnections = new LinkedList<>();

        for (int y = 0; y < clbHeight; y++) {
            List<List<Vertex>> rowTopConnections = new LinkedList<>();
            List<List<Vertex>> rowBottomConnections = new LinkedList<>();
            makeLeftSide(graph, switchblockEdge, clb, logicCellsPerCLB, wireCount, clbComponents[0][y], rowTopConnections, rowBottomConnections);
            for (int x = 1; x < clbWidth; x++) {
                makeMiddle(graph, switchblockMiddle, clb, logicCellsPerCLB, wireCount, clbComponents[x-1][y], clbComponents[x][y], rowTopConnections, rowBottomConnections);
            }
            makeRightSide(graph, switchblockEdge, clb, logicCellsPerCLB, wireCount, clbComponents[clbComponents.length-1][y], rowTopConnections, rowBottomConnections);
            topConnections.add(rowTopConnections);
            bottomConnections.add(rowBottomConnections);
        }
        for (int y = 1; y < clbHeight; y++) {
            for (int x = 0; x < Util.concat(topConnections.get(y)).size(); x++) {
                graph.addEdge(Util.concat(topConnections.get(y)).get(x), Util.concat(bottomConnections.get(y - 1)).get(x));
            }
        }

        makeTop(graph, switchblockEdge, topConnections);
        makeBottom(graph, switchblockEdge, bottomConnections);
        assert  clbWidth == bottomConnections.get(0).size() - 1;
        assert  wireCount == bottomConnections.get(0).get(0).size();
        if (write) {
            Writer.export(graph, true, Paths.get("src/test/resources/graphml/CLBFPGA"));
        }
        HierarchyGraph toReturn = graph.deepCopy().getGraph();
        FPGAModels.recursiveRemove(toReturn, x -> x.getLabels().contains(Label.REMOVE));
        return toReturn;
    }

    private static void makeBottom(HierarchyGraph graph, Switchblock switchblockEdge, List<List<List<Vertex>>> bottomConnections) {
        List<Vertex> previousBottomRight = null;
        int clbWidth = bottomConnections.get(0).size() - 1;
        int wireCount = bottomConnections.get(0).get(0).size();
        for (int i = 1; i < clbWidth; i++) {//i is the index of the intermediate vertical channel
            Vertex belowComponent = graph.addComponent(switchblockEdge.graph, "Switch edge");
            List<Vertex> belowLeft = switchblockEdge.right.stream().map(x -> graph.addPort(x, belowComponent)).collect(Collectors.toList());
            List<Vertex> belowRight = switchblockEdge.left.stream().map(x -> graph.addPort(x, belowComponent)).collect(Collectors.toList());
            List<Vertex> belowTop = switchblockEdge.bottom.stream().map(x -> graph.addPort(x, belowComponent)).collect(Collectors.toList());
            if (i == 1) {
                for (int wire = 0; wire < wireCount; wire++) {
                    graph.addEdge(belowLeft.get(wire), bottomConnections.get(bottomConnections.size()-1).get(0).get(wire));
                }
            }
            if (i == clbWidth - 1) {
                for (int wire = 0; wire < wireCount; wire++) {
                    List<List<Vertex>> bottomrow = bottomConnections.get(bottomConnections.size()-1);
                    graph.addEdge(belowRight.get(wire), bottomrow.get(bottomrow.size()-1).get(wire));
                }
            }
            for (int wire = 0; wire < wireCount; wire++) {
                graph.addEdge(belowTop.get(wire), bottomConnections.get(bottomConnections.size()-1).get(i).get(wire));
            }
            if (i > 1) {
                for (int wire = 0; wire < wireCount; wire++) {
                    graph.addEdge(previousBottomRight.get(wire), belowRight.get(wire));
                }
            }
            previousBottomRight = belowRight;
        }
    }

    private static void makeTop(HierarchyGraph graph, Switchblock switchblockEdge, List<List<List<Vertex>>> topConnections) {
        List<Vertex> previousTopRight = null;
        int clbWidth = topConnections.get(0).size() - 1;
        int wireCount = topConnections.get(0).get(0).size();
        for (int i = 1; i < clbWidth; i++) {//i is the index of the intermediate vertical channel
            Vertex aboveComponent = graph.addComponent(switchblockEdge.graph, "Switch edge");
            List<Vertex> aboveLeft = switchblockEdge.left.stream().map(x -> graph.addPort(x, aboveComponent)).collect(Collectors.toList());
            List<Vertex> aboveRight = switchblockEdge.right.stream().map(x -> graph.addPort(x, aboveComponent)).collect(Collectors.toList());
            List<Vertex> aboveBottom = switchblockEdge.bottom.stream().map(x -> graph.addPort(x, aboveComponent)).collect(Collectors.toList());
            if (i == 1) {
                for (int wire = 0; wire < wireCount; wire++) {
                    graph.addEdge(aboveLeft.get(wire), topConnections.get(0).get(0).get(wire));
                }
            }
            if (i == clbWidth - 1) {
                for (int wire = 0; wire < wireCount; wire++) {
                    graph.addEdge(aboveRight.get(wire), topConnections.get(0).get(topConnections.get(0).size()-1).get(wire));
                }
            }
            for (int wire = 0; wire < wireCount; wire++) {
                graph.addEdge(aboveBottom.get(wire), topConnections.get(0).get(i).get(wire));
            }
            if (i > 1) {
                for (int wire = 0; wire < wireCount; wire++) {
                    graph.addEdge(previousTopRight.get(wire), aboveRight.get(wire));
                }
            }
            previousTopRight = aboveRight;
        }

    }

    private static void makeRightSide(HierarchyGraph graph, Switchblock switchblockEdge, ConfigurableLogicBlock clb, int logicCellsPerCLB, int wireCount, Vertex clbComponent, List<List<Vertex>> rowTopConnections, List<List<Vertex>> rowBottomConnections) {
        List<Vertex> bottomright = null;
        for (int i = 0; i < logicCellsPerCLB; i++) {
            Vertex component = graph.addComponent(switchblockEdge.graph, "Switch edge");
            List<Vertex> leftPorts = switchblockEdge.bottom.stream().map(vertex -> graph.addPort(vertex, component)).collect(Collectors.toList());
            List<Vertex> topPorts = switchblockEdge.left.stream().map(vertex -> graph.addPort(vertex, component)).collect(Collectors.toList());
            List<Vertex> bottomPorts = switchblockEdge.right.stream().map(vertex -> graph.addPort(vertex, component)).collect(Collectors.toList());
            for (int wire = 0; wire < wireCount; wire++) {
                graph.addEdge(leftPorts.get(wire), graph.addPort(clb.outputs.get(i).get(wire), clbComponent));
            }
            if (i==0) {
                rowTopConnections.add(topPorts);
            } else {
                for (int wire = 0; wire < wireCount; wire++) {
                    graph.addEdge(topPorts.get(wire), bottomright.get(wire));
                }
            }
            bottomright = bottomPorts;
        }
        rowBottomConnections.add(bottomright);
    }

    private static void makeMiddle(HierarchyGraph graph, Switchblock switchblockMiddle, ConfigurableLogicBlock clb, int logicCellsPerCLB, int wireCount, Vertex componentLeft, Vertex componentRight, List<List<Vertex>> rowTopConnections, List<List<Vertex>> rowBottomConnections) {
        List<Vertex> bottom = null;
        for (int i = 0; i < logicCellsPerCLB; i++) {
            Vertex component = graph.addComponent(switchblockMiddle.graph, "Switch block");
            List<Vertex> rightPorts = switchblockMiddle.right.stream().map(vertex -> graph.addPort(vertex, component)).collect(Collectors.toList());
            List<Vertex> leftPorts = switchblockMiddle.left.stream().map(vertex -> graph.addPort(vertex, component)).collect(Collectors.toList());
            List<Vertex> topPorts = switchblockMiddle.top.stream().map(vertex -> graph.addPort(vertex, component)).collect(Collectors.toList());
            List<Vertex> bottomPorts = switchblockMiddle.bottom.stream().map(vertex -> graph.addPort(vertex, component)).collect(Collectors.toList());
            for (int wire = 0; wire < wireCount; wire++) {
                graph.addEdge(rightPorts.get(wire), graph.addPort(clb.inputs.get(i).get(wire), componentRight));
                graph.addEdge(leftPorts.get(wire), graph.addPort(clb.outputs.get(i).get(wire), componentLeft));
            }
            if (i==0) {
                rowTopConnections.add(topPorts);
            } else {
                for (int wire = 0; wire < wireCount; wire++) {
                    graph.addEdge(topPorts.get(wire), bottom.get(wire));
                }
            }
            bottom = bottomPorts;
        }
        rowBottomConnections.add(bottom);
    }


    private static void makeLeftSide(HierarchyGraph graph, Switchblock triJump, ConfigurableLogicBlock clb, int logicCellsPerCLB, int wireCount, Vertex clbComponent, List<List<Vertex>> topConnections, List<List<Vertex>> bottomConnections) {
        List<Vertex> bottomleft = null;
        for (int i = 0; i < logicCellsPerCLB; i++) {
            Vertex component = graph.addComponent(triJump.graph, "Switch edge");
            List<Vertex> rightPorts = triJump.bottom.stream().map(vertex -> graph.addPort(vertex, component)).collect(Collectors.toList());
            List<Vertex> topPorts = triJump.right.stream().map(vertex -> graph.addPort(vertex, component)).collect(Collectors.toList());
            List<Vertex> bottomPorts = triJump.left.stream().map(vertex -> graph.addPort(vertex, component)).collect(Collectors.toList());
            for (int wire = 0; wire < wireCount; wire++) {
                graph.addEdge(rightPorts.get(wire), graph.addPort(clb.inputs.get(i).get(wire), clbComponent));
            }
            if (i==0) {
                topConnections.add(topPorts);
            } else {
                for (int wire = 0; wire < wireCount; wire++) {
                    graph.addEdge(topPorts.get(wire), bottomleft.get(wire));
                }
            }
            bottomleft = bottomPorts;
        }
        bottomConnections.add(bottomleft);
    }




}
