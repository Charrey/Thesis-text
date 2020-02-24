package testMaker;

import data.graph.HierarchyGraph;
import data.graph.Label;
import data.graph.Node;
import data.graph.Patterns;
import exceptions.ParseException;
import org.eclipse.collections.impl.tuple.Tuples;
import reader.Reader;
import writer.Writer;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MakeTests {

    public static void main(String[] args) throws IOException, ParserConfigurationException, ParseException {
        makeSimpleLut();
        makeSimpleRegister();
        makeSimpleMux();
        makeSimpleRoutingSwitch();
        makeSimpleLogicCell();

        Path mainFile = Paths.get("src/test/resources/graphml/LogicCell");
        HierarchyGraph graph = new Reader().read(mainFile.resolve("main.graphml"));
        System.out.println(graph.flatten().toDOT(false));
    }

    private static void makeSimpleRoutingSwitch() throws IOException {
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
    }

    private static void makeSimpleMux() throws IOException {
        Patterns.MUX mux = Patterns.MUX(1);
        HierarchyGraph res = new HierarchyGraph();
        Node component = res.addComponent(mux.hierarchyGraph, "MUX");
        List<Node> pins = Patterns.addPins(res, 4);
        List<Node> ports = Patterns.addPorts(res, 4);
        for (int i = 0; i < ports.size(); i++) {
            res.addEdge(ports.get(i), component);
        }
        for (int i = 0; i < pins.size(); i++) {
            res.addEdge(pins.get(i), ports.get(i));
        }
        res.addPortMapping(ports.get(0), mux.in1.get(0));
        res.addPortMapping(ports.get(1), mux.in2.get(0));
        res.addPortMapping(ports.get(2), mux.out.get(0));
        res.addPortMapping(ports.get(3), mux.select);
        Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/MUX"));
    }

    private static void makeSimpleLogicCell() throws IOException {
        HierarchyGraph graph = new HierarchyGraph();

        Node pin_in_1 = graph.addNode(Label.PIN);
        Node pin_in_2 = graph.addNode(Label.PIN);
        Node pin_out_1 = graph.addNode(Label.PIN);
        Node pin_out_2 = graph.addNode(Label.PIN);
        Node clock = graph.addNode(Label.CLOCK);
        Node on = graph.addNode(Label.ALWAYS_ON);

        Patterns.Switch mySwitch1 = Patterns.Switch();
        mySwitch1.addOption(Tuples.twin(0, 1), Tuples.twin(1, 0));
        mySwitch1.addOption(Tuples.twin(0, 2), Tuples.twin(2, 0));
        Node switch1Component = graph.addComponent(mySwitch1.getHierarchyGraph(), "Switch 1");
        Node switch1Port1 = graph.addPort(mySwitch1.getByNumber(0), switch1Component);
        Node switch1Port2 = graph.addPort(mySwitch1.getByNumber(1), switch1Component);
        Node switch1Port3 = graph.addPort(mySwitch1.getByNumber(2), switch1Component);

        graph.addEdge(pin_in_1, switch1Port1);

        Node switch2Component = graph.addComponent(mySwitch1.getHierarchyGraph(), "Switch");
        Node switch2Port1 = graph.addPort(mySwitch1.getByNumber(0), switch2Component);
        assert graph.getPortMapping().get(switch1Port1).equals(graph.getPortMapping().get(switch2Port1));
        Node switch2Port2 = graph.addPort(mySwitch1.getByNumber(1), switch2Component);
        Node switch2Port3 = graph.addPort(mySwitch1.getByNumber(2), switch2Component);
        graph.addEdge(pin_in_2, switch2Port1);

        Patterns.LUT LUT = Patterns.LUT(2, 2);
        Node LUTComponent = graph.addComponent(LUT.hierarchyGraph, "LUT");
        Node LUTPort1 = graph.addPort(LUT.inputs.get(0), LUTComponent);
        Node LUTPort2 = graph.addPort(LUT.inputs.get(1), LUTComponent);
        Node LUTPort3 = graph.addPort(LUT.outputs.get(0), LUTComponent);
        Node LUTPort4 = graph.addPort(LUT.outputs.get(1), LUTComponent);

        graph.addEdge(LUTPort1, switch1Port2);
        graph.addEdge(LUTPort2, switch2Port2);

        Patterns.Register register = Patterns.Register(2, false, false);
        Node registerComponent = graph.addComponent(register.hierarchyGraph, "Register");
        Node RegisterPort1 = graph.addPort(register.inputs.get(0), registerComponent);
        Node RegisterPort2 = graph.addPort(register.inputs.get(1), registerComponent);
        Node RegisterPort3 = graph.addPort(register.outputs.get(0), registerComponent);
        Node RegisterPort4 = graph.addPort(register.outputs.get(1), registerComponent);
        Node RegisterPort5 = graph.addPort(register.set, registerComponent);
        graph.addEdge(clock, RegisterPort5);
        graph.addEdge(RegisterPort1, switch1Port3);
        graph.addEdge(RegisterPort2, switch2Port3);

        Patterns.MUX MUX = Patterns.MUX(2);
        Node muxComponent = graph.addComponent(MUX.hierarchyGraph, "MUX");
        Node MUXPort1 = graph.addPort(MUX.in1.get(0), muxComponent);
        Node MUXPort2 = graph.addPort(MUX.in1.get(1), muxComponent);
        Node MUXPort3 = graph.addPort(MUX.in2.get(0), muxComponent);
        Node MUXPort4 = graph.addPort(MUX.in2.get(1), muxComponent);
        Node MUXPort5 = graph.addPort(MUX.out.get(0), muxComponent);
        Node MUXPort6 = graph.addPort(MUX.out.get(1), muxComponent);
        Node MUXPort7 = graph.addPort(MUX.select, muxComponent);
        graph.addEdge(MUXPort1, LUTPort3);
        graph.addEdge(MUXPort2, LUTPort4);
        graph.addEdge(MUXPort3, RegisterPort3);
        graph.addEdge(MUXPort4, RegisterPort4);
        graph.addEdge(MUXPort5, pin_out_1);
        graph.addEdge(MUXPort6, pin_out_2);
        graph.addEdge(MUXPort7, on);

        Writer.writeToDirectory(Writer.export(graph, true), Paths.get("src/test/resources/graphml/LogicCell"));
    }

    private static void makeSimpleRegister() throws IOException {
        Patterns.Register register = Patterns.Register(1, true, true);
        HierarchyGraph res = new HierarchyGraph();

        Node component = res.addComponent(register.hierarchyGraph, "Register");
        List<Node> pins = Patterns.addPins(res, 5);
        List<Node> ports = Patterns.addPorts(res, 5);
        for (int i = 0; i < ports.size(); i++) {
            res.addEdge(ports.get(i), component);
        }
        for (int i = 0; i < pins.size(); i++) {
            res.addEdge(pins.get(i), ports.get(i));
        }
        res.addPortMapping(ports.get(0), register.inputs.get(0));
        res.addPortMapping(ports.get(1), register.outputs.get(0));
        res.addPortMapping(ports.get(2), register.set);
        res.addPortMapping(ports.get(3), register.syncReset);
        res.addPortMapping(ports.get(4), register.asyncReset);
        Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/Register"));
    }

    private static void makeSimpleLut() throws IOException {
        Patterns.LUT lut = Patterns.LUT(2, 2);
        HierarchyGraph res = new HierarchyGraph();

        Node component = res.addComponent(lut.hierarchyGraph, "LUT");
        List<Node> pins = Patterns.addPins(res, 4);
        List<Node> ports = Patterns.addPorts(res, 4);
        for (int i = 0; i < pins.size(); i++) {
            res.addEdge(pins.get(i), component);
        }
        for (int i = 0; i < ports.size(); i++) {
            res.addEdge(ports.get(i), pins.get(i));
        }
        res.addPortMapping(ports.get(0), lut.inputs.get(0));
        res.addPortMapping(ports.get(1), lut.inputs.get(1));
        res.addPortMapping(ports.get(2), lut.outputs.get(0));
        res.addPortMapping(ports.get(3), lut.outputs.get(1));
        Writer.writeToDirectory(Writer.export(res, true), Paths.get("src/test/resources/graphml/LUT"));
    }
}
