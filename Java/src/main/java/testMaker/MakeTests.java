package testMaker;

import data.graph.HierarchyGraph;
import data.graph.Label;
import data.graph.Node;
import data.graph.Patterns;
import org.eclipse.collections.impl.tuple.Tuples;
import util.Util;
import writer.Writer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class MakeTests {

    public static void main(String[] args) throws IOException {
        makeLutAndRegister(3);
        makeSimpleLut(2);
        makeSimpleRegister(2);
        makeSimpleMux(1);
        makeSimpleRoutingSwitch();
        makeSimpleLogicCell(3);

    }

    public static HierarchyGraph makeLutAndRegister(int ins) throws IOException {
        HierarchyGraph res = new HierarchyGraph();
        Patterns.Register register = Patterns.Register(ins, false, false);
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

        Patterns.LUT lut = Patterns.LUT(ins + 1, ins);
        pins = new LinkedList<>();
        ports = new LinkedList<>();
        component = res.addComponent(lut.hierarchyGraph, "LUT");
        res.addEdge(clock, res.addPort(lut.inputs.get(2), component));
        for (int i = 0; i < ins; i++) {
            pins.add(res.addNode(Label.PIN));
            ports.add(res.addPort(lut.inputs.get(i), component));
        }
        for (int i = 0; i < ins; i++) {
            pins.add(res.addNode(Label.PIN));
            ports.add(res.addPort(lut.outputs.get(i), component));
        }
        for (int i = 0; i < pins.size(); i++) {
            res.addEdge(pins.get(i), ports.get(i));
        }
        Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/LutAndRegister"));
        return res;
    }

    public static HierarchyGraph makeSimpleRoutingSwitch() throws IOException {
        Patterns.Switch mySwitch = Patterns.Switch();
        mySwitch.addOption(Tuples.twin(0, 1), Tuples.twin(1, 0));
        mySwitch.addOption(Tuples.twin(0, 2), Tuples.twin(2, 0));
        HierarchyGraph res = new HierarchyGraph();
        Node component = res.addComponent(mySwitch.getHierarchyGraph(), "switch");
        List<Node> pins = Patterns.addPins(res, 3);
        List<Node> ports = Patterns.addPorts(res, 3);
        for (int i = 0; i < pins.size(); i++) {
            res.addEdge(ports.get(i), component);
            res.addEdge(pins.get(i), ports.get(i));
        }
        res.addPortMapping(ports.get(0), mySwitch.getByNumber(0));
        res.addPortMapping(ports.get(1), mySwitch.getByNumber(1));
        res.addPortMapping(ports.get(2), mySwitch.getByNumber(2));
        Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/Switch"));
        return res;
    }

    public static HierarchyGraph makeSimpleMux(int wireCount) throws IOException {
        Patterns.MUX mux = Patterns.MUX(wireCount);
        HierarchyGraph res = new HierarchyGraph();
        Node component = res.addComponent(mux.hierarchyGraph, "MUX");
        List<Node> pins = Patterns.addPins(res, 3*wireCount + 1);
        List<Node> ports = Patterns.addPorts(res, 3*wireCount + 1);
        for (int i = 0; i < ports.size(); i++) {
            res.addEdge(ports.get(i), component);
        }
        for (int i = 0; i < pins.size(); i++) {
            res.addEdge(pins.get(i), ports.get(i));
        }
        for (int i = 0; i < wireCount; i++) {
            res.addPortMapping(ports.get(i), Util.concat(mux.in1, mux.in2, mux.out, List.of(mux.select)).get(i));
        }
        res.addPortMapping(ports.get(0), mux.in1.get(0));
        res.addPortMapping(ports.get(1), mux.in2.get(0));
        res.addPortMapping(ports.get(2), mux.out.get(0));
        res.addPortMapping(ports.get(3), mux.select);
        Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/MUX"));
        return res;
    }

    public static HierarchyGraph makeSimpleLogicCell(int wires) throws IOException {
        HierarchyGraph graph = new HierarchyGraph();

        List<Node> pins = Patterns.addPins(graph, wires * 2);
        Node clock = graph.addNode(Label.CLOCK);
        Node on = graph.addNode(Label.EXTRA);

        Patterns.Switch mySwitch = Patterns.Switch();
        mySwitch.addOption(Tuples.twin(0, 1), Tuples.twin(1, 0));
        mySwitch.addOption(Tuples.twin(0, 2), Tuples.twin(2, 0));

        Patterns.LUT LUT = Patterns.LUT(wires, wires);
        Patterns.Register register = Patterns.Register(wires, false, false);
        Patterns.MUX MUX = Patterns.MUX(wires);

        Node LUTComponent = graph.addComponent(LUT.hierarchyGraph, "LUT");
        Node registerComponent = graph.addComponent(register.hierarchyGraph, "Register");
        Node muxComponent = graph.addComponent(MUX.hierarchyGraph, "MUX");


        for (int i = 0; i < wires; i++) {
            Node switch1Component = graph.addComponent(mySwitch.getHierarchyGraph(), "Switch 1");
            graph.addEdge(pins.get(i),                                              graph.addPort(mySwitch.getByNumber(0), switch1Component));
            graph.addEdge(graph.addPort(LUT.inputs.get(i), LUTComponent),           graph.addPort(mySwitch.getByNumber(1), switch1Component));
            graph.addEdge(graph.addPort(register.inputs.get(i), registerComponent), graph.addPort(mySwitch.getByNumber(2), switch1Component));
            graph.addEdge(graph.addPort(MUX.in1.get(i), muxComponent), graph.addPort(LUT.outputs.get(i), LUTComponent));
            graph.addEdge(graph.addPort(MUX.in2.get(i), muxComponent), graph.addPort(register.outputs.get(i), registerComponent));
            graph.addEdge(graph.addPort(MUX.out.get(i), muxComponent),      pins.get(i + wires));
        }
        graph.addEdge(clock, graph.addPort(register.set, registerComponent));
        graph.addEdge(on, graph.addPort(MUX.select, muxComponent));
        Writer.writeToDirectory(Writer.export(graph, true), Paths.get("src/test/resources/graphml/LogicCell"));
        return graph;
    }

    public static HierarchyGraph makeSimpleRegister(int wireCount) throws IOException {
        Patterns.Register register = Patterns.Register(wireCount, true, true);
        HierarchyGraph res = new HierarchyGraph();

        Node component = res.addComponent(register.hierarchyGraph, "Register");
        List<Node> pins = Patterns.addPins(res, 2*wireCount + 3);
        List<Node> ports = Patterns.addPorts(res, 2*wireCount + 3);
        for (int i = 0; i < 2*wireCount + 3; i++) {
            res.addEdge(ports.get(i), component);
            res.addEdge(pins.get(i), ports.get(i));
            res.addPortMapping(ports.get(i), Util.concat(register.inputs, register.outputs, List.of(register.set), List.of(register.syncReset), List.of(register.asyncReset)).get(i));
        }
        Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/Register"));
        return res;
    }

    public static HierarchyGraph makeSimpleLut(int ins) throws IOException {
        Patterns.LUT lut = Patterns.LUT(ins, ins);
        HierarchyGraph res = new HierarchyGraph();

        Node component = res.addComponent(lut.hierarchyGraph, "LUT");
        List<Node> pins = Patterns.addPins(res, ins*2);
        List<Node> ports = Patterns.addPorts(res, ins*2);
        for (int i = 0; i < pins.size(); i++) {
            res.addEdge(ports.get(i), component);
        }
        for (int i = 0; i < ports.size(); i++) {
            res.addEdge(ports.get(i), pins.get(i));
            res.addPortMapping(ports.get(i), Util.concat(lut.inputs, lut.outputs).get(i));
        }
        Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/LUT"));
        return res;
    }
}
