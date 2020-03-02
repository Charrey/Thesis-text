package testMaker;

import data.graph.HierarchyGraph;
import data.graph.Label;
import data.graph.Node;
import data.graph.Patterns;
import data.patterns.*;
import util.Util;
import writer.Writer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

public class FPGAModels {

    public static void main(String[] args) throws IOException {
        makeLutAndRegister(3, true);
        makeSimpleLut(2, true);
        makeSimpleRegister(2, true);
        makeSimpleMux(1, true);
        makeSimpleRoutingSwitch(true);
        makeSimpleLogicCell(3, true);

    }

    public static HierarchyGraph makeLutAndRegister(int ins, boolean write) throws IOException {
        HierarchyGraph res = new HierarchyGraph();
        Register register = Patterns.Register(ins, false, false);
        List<Node> pins = new LinkedList<>();
        List<Node> ports = new LinkedList<>();
        Node clock = res.addNode(Label.CLOCK);
        Node component = res.addComponent(register.hierarchyGraph, "Register");
        res.addEdge(clock, res.addPort(register.set, component));
        for (int i = 0; i < ins; i++) {
            pins.add(res.addNode(Label.PIN));
            ports.add(res.addPort(register.inputs.get(i), component));
        }
        for (int i = 0; i < ins; i++) {
            pins.add(res.addNode(Label.PIN));
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
            pins.add(res.addNode(Label.PIN));
            ports.add(res.addPort(lut.getInputs().get(i), component));
        }
        for (int i = 0; i < ins; i++) {
            pins.add(res.addNode(Label.PIN));
            ports.add(res.addPort(lut.getOutputs().get(i), component));
        }
        for (int i = 0; i < pins.size(); i++) {
            res.addEdge(pins.get(i), ports.get(i));
        }
        if (write) {
            Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/LutAndRegister"));
        }
        return res;
    }

    public static HierarchyGraph makeSimpleRoutingSwitch(boolean write) throws IOException {
        Switch mySwitch = Patterns.Switch();
        mySwitch.addOption(new Patterns.IntConnection(0, 1), new Patterns.IntConnection(1, 0));
        mySwitch.addOption(new Patterns.IntConnection(0, 2), new Patterns.IntConnection(2, 0));
        HierarchyGraph res = new HierarchyGraph();
        Node component = res.addComponent(mySwitch.getHierarchyGraph(), "switch");
        List<Node> pins = Patterns.addPins(res, 3);
        List<Node> ports = new LinkedList<>();//Patterns.addPorts(res, 3);
        for (int i = 0; i < pins.size(); i++) {
            ports.add(res.addPort(mySwitch.getByNumber(i), component));
            res.addEdge(ports.get(i), component);
            res.addEdge(pins.get(i), ports.get(i));
        }
        if (write) {
            Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/Switch"));
        }
        return res;
    }

    public static HierarchyGraph makeSimpleMux(int wireCount, boolean write) throws IOException {
        MUX mux = Patterns.MUX(wireCount);
        HierarchyGraph res = new HierarchyGraph();
        Node component = res.addComponent(mux.hierarchyGraph, "MUX");
        List<Node> pins = Patterns.addPins(res, 3*wireCount + 1);
        List<Node> ports = new LinkedList<>();//Patterns.addPorts(res, 3*wireCount + 1);
        for (int i = 0; i < pins.size(); i++) {
            ports.add(res.addPort(Util.concat(mux.in1, mux.in2, mux.out, List.of(mux.select)).get(i), component));
        }
        for (int i = 0; i < pins.size(); i++) {
            res.addEdge(pins.get(i), ports.get(i));
        }
        if (write) {
            Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/MUX"));
        }
        return res;
    }

    public static HierarchyGraph makeSimpleLogicCell(int wires, boolean write) throws IOException {
        HierarchyGraph graph = new HierarchyGraph();

        List<Node> pins = Patterns.addPins(graph, wires * 2);
        Node clock = graph.addNode(Label.CLOCK);
        Node on = graph.addNode(Label.EXTRA);

        Switch mySwitch = Patterns.Switch();
        mySwitch.addOption(new Patterns.IntConnection(0, 1), new Patterns.IntConnection(1, 0));
        mySwitch.addOption(new Patterns.IntConnection(0, 2), new Patterns.IntConnection(2, 0));

        LUT LUT = Patterns.LUT(wires, wires);
        Register register = Patterns.Register(wires, false, false);
        MUX MUX = Patterns.MUX(wires);

        Node LUTComponent = graph.addComponent(LUT.getHierarchyGraph(), "LUT");
        Node registerComponent = graph.addComponent(register.hierarchyGraph, "Register");
        Node muxComponent = graph.addComponent(MUX.hierarchyGraph, "MUX");


        for (int i = 0; i < wires; i++) {
            Node switch1Component = graph.addComponent(mySwitch.getHierarchyGraph(), "Switch 1");
            graph.addEdge(pins.get(i),                                              graph.addPort(mySwitch.getByNumber(0), switch1Component));
            graph.addEdge(graph.addPort(LUT.getInputs().get(i), LUTComponent),           graph.addPort(mySwitch.getByNumber(1), switch1Component));
            graph.addEdge(graph.addPort(register.inputs.get(i), registerComponent), graph.addPort(mySwitch.getByNumber(2), switch1Component));
            graph.addEdge(graph.addPort(MUX.in1.get(i), muxComponent), graph.addPort(LUT.getOutputs().get(i), LUTComponent));
            graph.addEdge(graph.addPort(MUX.in2.get(i), muxComponent), graph.addPort(register.outputs.get(i), registerComponent));
            graph.addEdge(graph.addPort(MUX.out.get(i), muxComponent),      pins.get(i + wires));
        }
        graph.addEdge(clock, graph.addPort(register.set, registerComponent));
        graph.addEdge(on, graph.addPort(MUX.select, muxComponent));
        if (write) {
            Writer.writeToDirectory(Writer.export(graph, true), Paths.get("src/test/resources/graphml/LogicCell"));
        }
        return graph;
    }

    public static HierarchyGraph makeSimpleRegister(int wireCount, boolean write) throws IOException {
        Register register = Patterns.Register(wireCount, true, true);
        HierarchyGraph res = new HierarchyGraph();

        Node component = res.addComponent(register.hierarchyGraph, "Register");
        List<Node> pins = Patterns.addPins(res, 2*wireCount + 3);
        List<Node> ports = new LinkedList<>();//Patterns.addPorts(res, 2*wireCount + 3);
        for (int i = 0; i < 2*wireCount + 3; i++) {
            ports.add(res.addPort(Util.concat(register.inputs, register.outputs, List.of(register.set), List.of(register.syncReset), List.of(register.asyncReset)).get(i), component));
            res.addEdge(ports.get(i), component);
            res.addEdge(pins.get(i), ports.get(i));
        }
        if (write) {
            Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/Register"));
        }
        return res;
    }

    public static HierarchyGraph makeSimpleLut(int ins, boolean write) throws IOException {
        LUT lut = Patterns.LUT(ins, ins);
        HierarchyGraph res = new HierarchyGraph();

        Node component = res.addComponent(lut.getHierarchyGraph(), "LUT");
        List<Node> pins = Patterns.addPins(res, ins*2);
        List<Node> ports = new LinkedList<>();//Patterns.addPorts(res, ins*2);
        for (int i = 0; i < pins.size(); i++) {
            ports.add(res.addPort(Util.concat(lut.getInputs(), lut.getOutputs()).get(i), component));
            res.addEdge(ports.get(i), component);
        }
        for (int i = 0; i < ports.size(); i++) {
            res.addEdge(ports.get(i), pins.get(i));
        }
        if (write) {
            Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/LUT"));
        }
        return res;
    }

    public static HierarchyGraph makeSnake(int width, int length, boolean write) throws IOException {
        assert width > 0;
        HierarchyGraph graph = new HierarchyGraph();
        Node[][] components = new Node[width][length];

        Node[][] leftPorts = new Node[width][length];
        Node[][] rightPorts = new Node[width][length];
        Node[][] upPorts = new Node[width - 1][length];
        Node[][] downPorts = new Node[width - 1][length];

        List<Node> inputPins = Patterns.addPins(graph, width);
        List<Node> outputPins = Patterns.addPins(graph, width);

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
            Writer.writeToDirectory(Writer.export(graph, true), Paths.get("src/test/resources/graphml/LUT"));
        }
        return graph;
    }

    public static HierarchyGraph makeLutTree(int smallLutWires, int bigLutWires, boolean write) throws IOException {
        HierarchyGraph res =  getLutTree(smallLutWires, bigLutWires).getHierarchyGraph().deepCopy().getGraph().flatten().getGraph();
        for (Node node : res.getNodesByLabel(Label.REMOVE)) {
            if (res.getEdges().get(node).size()==1) {
                res.addEdge(node, res.addNode(Label.PIN));
            }
        }
        recursiveRemove(res, x -> x.getLabels().contains(Label.REMOVE));
        return res;

    }

    private static void recursiveRemove(HierarchyGraph res, Predicate<Node> predicate) {
        while (res.getNodes().stream().anyMatch(predicate)) {
            Node example = res.getNodes().stream().filter(predicate).findAny().get();
            for (Node one : res.getEdges().get(example)) {
                for (Node two : res.getEdges().get(example)) {
                    if (two.getID() > one.getID()) {
                        res.addEdge(one, two);
                    }
                }
            }
            res.removeNode(example);
        }
        for (HierarchyGraph graph : res.getHierarchy().values()) {
            recursiveRemove(graph, predicate);
        }
    }

    private static LUT getLutTree(int smallLutWires, int bigLutWires) throws IOException {
        if (smallLutWires == bigLutWires) {
            return Patterns.LUT(smallLutWires, 4);
        } else {
            HierarchyGraph res = new HierarchyGraph();
            LUT oneSmaller = getLutTree(smallLutWires, bigLutWires - 1);
            MUX mux = Patterns.MUX(smallLutWires);
            Node LutComponent1 = res.addComponent(oneSmaller.getHierarchyGraph(), "LowerLut");
            Node LutComponent2 = res.addComponent(oneSmaller.getHierarchyGraph(), "LowerLut");
            Node MuxComponent = res.addComponent(mux.hierarchyGraph, "MUX");
            List<Node> upperLutInPorts = new LinkedList<>();
            List<Node> lowerLutInPorts = new LinkedList<>();
            List<Node> upperLutOutPorts = new LinkedList<>();
            List<Node> lowerLutOutPorts = new LinkedList<>();
            List<Node> muxUpperInPorts = new LinkedList<>();
            List<Node> muxLowerInPorts = new LinkedList<>();
            List<Node> muxOutPorts = new LinkedList<>();

            List<Node> lutTreeOutPorts = new LinkedList<>();
            List<Node> lutTreeInPorts = new LinkedList<>();
            /*
                   /|---/\---
            ------| |---\/---
                  | |
            ------| |---/\---
                  |\|---\/---
            -----/mux  luttreex2
             */
            //Connect outputs of LUTs to this FPGA
            for (Node subNode : oneSmaller.getOutputs()) {
                upperLutOutPorts.add(res.addPort(subNode, LutComponent1));
                lowerLutOutPorts.add(res.addPort(subNode, LutComponent2));
            }
            //Connect inputs of LUTs to this FPGA
            for (Node subNode : oneSmaller.getInputs()) {
                upperLutInPorts.add(res.addPort(subNode, LutComponent1));
                lowerLutInPorts.add(res.addPort(subNode, LutComponent2));
            }
            //Connect upper input of mux to FPGA
            for (Node subNode : mux.in1) {
                muxUpperInPorts.add(res.addPort(subNode, MuxComponent));
            }
            //Connect lower input of mux to FPGA
            for (Node subNode : mux.in2) {
                muxLowerInPorts.add(res.addPort(subNode, MuxComponent));
            }
            //Connect outputs of mux to FPGA
            for (Node subNode : mux.out) {
                muxOutPorts.add(res.addPort(subNode, MuxComponent));
            }
            //Connect selector of mux to FPGA
            Node selectPort = res.addPort(mux.select, MuxComponent);
            for (int i = 0; i < smallLutWires; i++) {
                res.addEdge(upperLutOutPorts.get(i), muxUpperInPorts.get(i));
                res.addEdge(lowerLutOutPorts.get(i), muxLowerInPorts.get(i));
            }
            for (Node inputPort : Util.concat(upperLutInPorts, lowerLutInPorts, List.of(selectPort))) {
                Node toAdd = res.addNode(Label.REMOVE);
                res.addEdge(toAdd, inputPort);
                lutTreeInPorts.add(toAdd);
            }
            for (Node outputPort : muxOutPorts) {
                Node toAdd = res.addNode(Label.REMOVE);
                res.addEdge(toAdd, outputPort);
                lutTreeOutPorts.add(toAdd);
            }
            return new LeafLUT(res, lutTreeInPorts, lutTreeOutPorts);
        }
    }

}
