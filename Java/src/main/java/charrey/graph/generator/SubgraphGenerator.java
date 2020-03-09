package charrey.data.graph;

import charrey.data.patterns.*;
import charrey.util.Util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that creates graph patterns for subcomponents.
 */
public final class Patterns {


    private static Map<Integer, MUX> muxMap = new HashMap<>();

    /**
     * Creates a MUX Hierarchygraph.
     *
     * @param inCount The number of wires each input has (and thus the number of wires the output has).
     * @return A MUX Object containing the created Hierarchygraph.
     */
    public static MUX MUX(int inCount) {
        if (muxMap.containsKey(inCount)) {
            return muxMap.get(inCount);
        }
        HierarchyGraph res = new HierarchyGraph();
        Vertex mux1 = res.addVertex(Label.MUX);
        Vertex mux2 = res.addVertex(Label.MUX);
        List<Vertex> topInputs = new LinkedList<>();
        List<Vertex> bottomInputs = new LinkedList<>();
        List<Vertex> outputs = new LinkedList<>();
        for (int i = 0; i < inCount; i++) {
            Vertex in1 = res.addVertex(Label.IN);
            Vertex in2 = res.addVertex(Label.IN);
            Vertex out = res.addVertex(Label.OUT);
            topInputs.add(in1);
            bottomInputs.add(in2);
            outputs.add(out);
            res.addEdge(in1, mux1);
            res.addEdge(in1, out);
            res.addEdge(in2, mux2);
            res.addEdge(in2, out);
        }
        Vertex select = res.addVertex(Label.SELECT);
        res.addEdge(select, mux1);
        res.addEdge(select, mux2);
        MUX result = new MUX(res, topInputs, bottomInputs, outputs, select);
        muxMap.put(inCount, result);
        return result;
    }

    private static Map<Integer, LogicCell> logicCellMap = new HashMap<>();

    /**
     * Logic cell logic cell.
     *
     * @param wireCount the wire count
     * @return the logic cell
     */
    public static LogicCell LogicCell(int wireCount) {
        if (logicCellMap.containsKey(wireCount)) {
            return logicCellMap.get(wireCount);
        }
        HierarchyGraph graph = new HierarchyGraph();

        Vertex extra = graph.addVertex(Label.EXTRA);

        Switch mySwitch = Patterns.Switch();
        mySwitch.addOption(new Patterns.IntConnection(0, 1), new Patterns.IntConnection(1, 0));
        mySwitch.addOption(new Patterns.IntConnection(0, 2), new Patterns.IntConnection(2, 0));

        LUT LUT = Patterns.LUT(wireCount, wireCount);
        Register register = Patterns.Register(wireCount, true, false, false, false, false);
        MUX MUX = Patterns.MUX(wireCount);

        Vertex LUTComponent = graph.addComponent(LUT.getHierarchyGraph(), "LUT");
        Vertex registerComponent = graph.addComponent(register.hierarchyGraph, "Register");
        Vertex muxComponent = graph.addComponent(MUX.hierarchyGraph, "MUX");

        List<Vertex> inputs = new LinkedList<>();
        List<Vertex> outputs = new LinkedList<>();
        for (int i = 0; i < wireCount; i++) {
            Vertex switch1Component = graph.addComponent(mySwitch.getHierarchyGraph(), "Switch 1");
            Vertex inputPort = graph.addPort(mySwitch.getByNumber(0), switch1Component);
            Vertex inputPortConnection = graph.addVertex(Label.REMOVE);
            graph.addEdge(inputPort, inputPortConnection);
            inputs.add(inputPortConnection);
            graph.addEdge(graph.addPort(LUT.getInputs().get(i), LUTComponent),           graph.addPort(mySwitch.getByNumber(1), switch1Component));
            graph.addEdge(graph.addPort(register.inputs.get(i), registerComponent), graph.addPort(mySwitch.getByNumber(2), switch1Component));
            graph.addEdge(graph.addPort(MUX.in1.get(i), muxComponent), graph.addPort(LUT.getOutputs().get(i), LUTComponent));
            graph.addEdge(graph.addPort(MUX.in2.get(i), muxComponent), graph.addPort(register.outputs.get(i), registerComponent));
            Vertex outputPort = graph.addPort(MUX.out.get(i), muxComponent);
            Vertex outputPortConnection = graph.addVertex(Label.REMOVE);
            graph.addEdge(outputPort, outputPortConnection);
            outputs.add(outputPortConnection);
        }
        graph.addEdge(extra, graph.addPort(MUX.select, muxComponent));
        Vertex clockPort = graph.addPort(register.syncSet, registerComponent);
        Vertex clockHelper = graph.addVertex(Label.REMOVE);
        graph.addEdge(clockHelper, clockPort);
        logicCellMap.put(wireCount, new LogicCell(graph, inputs, outputs, clockHelper));
        return logicCellMap.get(wireCount);
    }


    private static Map<Integer, Map<Integer, LUT>> lutMap = new HashMap<>();

    /**
     * Creates a LUT Hierarchygraph.
     *
     * @param inCount  The number of inputs of the LUT.
     * @param outCount The number of outputs of the LUT.
     * @return A LUT Object containing the created Hierarchygraph.
     */
    public static LUT LUT(int inCount, int outCount) {
        if (lutMap.containsKey(inCount) && lutMap.get(inCount).containsKey(outCount)) {
            return lutMap.get(inCount).get(outCount);
        }
        HierarchyGraph res = new HierarchyGraph();
        Vertex mux1 = res.addVertex(Label.MUX, Label.LUT);
        Vertex mux2 = res.addVertex(Label.MUX, Label.LUT);
        List<Vertex> inputs = new LinkedList<>();
        List<Vertex> outputs = new LinkedList<>();
        for (int i = 0; i < inCount; i++) {
            Vertex in1 = res.addVertex(Label.IN, Label.SELECT);
            inputs.add(in1);
            res.addEdge(in1, mux1);
            res.addEdge(in1, mux2);
        }
        for (int i = 0; i < outCount; i++) {
            Vertex out = res.addVertex(Label.OUT);
            outputs.add(out);
            for (Vertex input : inputs) {
                res.addEdge(input, out);
            }
        }
        LUT result = new LeafLUT(res, inputs, outputs);
        lutMap.putIfAbsent(inCount, new HashMap<>());
        lutMap.get(inCount).put(outCount, result);
        return result;
    }

    private static Map<Integer, Map<Boolean, Map<Boolean, Register>>> registerMap = new HashMap<>();

    /**
     * Creates a new register Hierarchygraph.
     *
     * @param wirecount   How many inputs- and outputs the register has
     * @param syncSet     Whether the register has a synchronous set input
     * @param asyncSet    Whether the register has an asynchronous set input
     * @param syncReset   Whether the register has a synchronous reset input
     * @param asyncReset  Whether the register has an asynchronous reset input
     * @param clockEnable Whether the register has a clock-enable input.
     * @return The register hierarchygraph.
     */
    public static Register Register(int wirecount, boolean syncSet, boolean asyncSet, boolean syncReset, boolean asyncReset, boolean clockEnable) {
        if (registerMap.containsKey(wirecount) && registerMap.get(wirecount).containsKey(syncReset) && registerMap.get(wirecount).get(syncReset).containsKey(asyncReset)) {
            return registerMap.get(wirecount).get(syncReset).get(asyncReset);
        }
        HierarchyGraph res = new HierarchyGraph();
        List<Vertex> ins = new ArrayList<>(wirecount);
        List<Vertex> outs = new ArrayList<>(wirecount);
        Util.assertOrElse(syncSet || asyncReset, "A register must have some set vertex.");
        Vertex central = res.addVertex(Label.REGISTER);
        //Node set = syncReset ? res.addNode(Label.SYNC_SET) : null;

        for (int i = 0; i < wirecount; i++) {
            Vertex in = res.addVertex(Label.IN);
            Vertex out = res.addVertex(Label.OUT);
            ins.add(in);
            outs.add(out);
            res.addEdge(in, out);
            res.addEdge(in, central);
        }
        Vertex syncResetNode = null;
        Vertex asyncResetNode = null;
        Vertex syncSetNode = null;
        Vertex asyncSetNode = null;
        Vertex clockEnableNode = null;
        if (asyncReset) {
            asyncResetNode = res.addVertex(Label.ASYNC_RESET);
            res.addEdge(asyncResetNode, central);
        }
        if (syncReset) {
            syncResetNode = res.addVertex(Label.SYNC_RESET);
            res.addEdge(syncResetNode, central);
        }
        if (syncSet) {
            syncSetNode = res.addVertex(Label.SYNC_SET);
            res.addEdge(syncSetNode, central);
        }
        if (asyncSet) {
            clockEnableNode = res.addVertex(Label.CLOCK_ENABLE);
            res.addEdge(clockEnableNode, central);
        }
        if (clockEnable) {
            asyncSetNode = res.addVertex(Label.ASYNC_SET);
            res.addEdge(asyncSetNode, central);
        }
        Register result = new Register(res, ins, outs, syncSetNode, asyncSetNode, syncResetNode, asyncResetNode, clockEnableNode);
        registerMap.putIfAbsent(wirecount, new HashMap<>());
        registerMap.get(wirecount).putIfAbsent(syncReset, new HashMap<>());
        registerMap.get(wirecount).get(syncReset).putIfAbsent(asyncReset, result);
        return result;
    }

    /**
     * Shortcut to add a number of pins to a graph and return the pins.
     *
     * @param graph The Hierarchygraph to add pins to
     * @param count The number of pins
     * @return List of pins added
     */
    public static List<Vertex> addPins(HierarchyGraph graph, int count) {
        List<Vertex> pins = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            pins.add(graph.addVertex(Label.PIN));
        }
        return pins;
    }

    /**
     * Creates a new Switch hierarchygraph.
     *
     * @return An object that can be configured to return a Hierarchygraph modeling any routing switch.
     */
    public static Switch Switch() {
        return new Switch();
    }

    /**
     * Gets a CLB that consists of two columns of logic blocks, connected horizontally with wires shifted down by one.
     *
     * @param wireCount      The number of inputs and outputs of each logic cell
     * @param logicCellCount The number of logic cells
     * @return An object that contains the hierarchygraph of this CLB.
     */
    public static ConfigurableLogicBlock getRectangleCLB(int wireCount, int logicCellCount) {


        HierarchyGraph graph = new HierarchyGraph();
        LogicCell logicCell = Patterns.LogicCell(wireCount);

        List<List<Vertex>> firstLayerInPorts = new LinkedList<>();
        List<Vertex> firstLayer = new LinkedList<>();
        List<List<Vertex>> firstLayerOutPorts = new LinkedList<>();

        List<List<Vertex>> secondLayerInPorts = new LinkedList<>();
        List<Vertex> secondLayer = new LinkedList<>();
        List<List<Vertex>> secondLayerOutPorts = new LinkedList<>();

        for (int i = 0; i < logicCellCount; i++) {
            firstLayer.add(graph.addComponent(logicCell.graph, "LogicCell"));
            secondLayer.add(graph.addComponent(logicCell.graph, "LogicCell"));
            firstLayerInPorts.add(new LinkedList<>());
            firstLayerOutPorts.add(new LinkedList<>());
            secondLayerInPorts.add(new LinkedList<>());
            secondLayerOutPorts.add(new LinkedList<>());
            for (int j = 0; j < wireCount; j++) {
                firstLayerInPorts.get(i).add(graph.addPort(logicCell.inputs.get(j), firstLayer.get(i)));
                firstLayerOutPorts.get(i).add(graph.addPort(logicCell.outputs.get(j), firstLayer.get(i)));
                secondLayerInPorts.get(i).add(graph.addPort(logicCell.inputs.get(j), secondLayer.get(i)));
                secondLayerOutPorts.get(i).add(graph.addPort(logicCell.outputs.get(j), secondLayer.get(i)));
                if (i > 0 || j > 0) {
                    int previousI = j == 0 ? i - 1 : i;
                    int previousJ = j == 0 ? wireCount - 1 : j - 1;
                    graph.addEdge(firstLayerOutPorts.get(previousI).get(previousJ), secondLayerInPorts.get(i).get(j));
                }
            }
        }
        List<List<Vertex>> inputs = firstLayerInPorts.stream().map(vertices -> vertices.stream().map(vertex -> {
            Vertex connection = graph.addVertex(Label.REMOVE);
            graph.addEdge(connection, vertex);
            return connection;
        }).collect(Collectors.toList())).collect(Collectors.toList());

        List<List<Vertex>> outputs = secondLayerOutPorts.stream().map(vertices -> vertices.stream().map(vertex -> {
            Vertex connection = graph.addVertex(Label.REMOVE);
            graph.addEdge(connection, vertex);
            return connection;
        }).collect(Collectors.toList())).collect(Collectors.toList());
        Set<Vertex> clocks = new HashSet<>();
        for (int i = 0; i < logicCellCount; i++) {
            Vertex clockPort1 = graph.addPort(logicCell.clockPort, firstLayer.get(i));
            Vertex clockPort2 = graph.addPort(logicCell.clockPort, secondLayer.get(i));
            Vertex clockPin1 = graph.addVertex(Label.REMOVE);
            Vertex clockPin2 = graph.addVertex(Label.REMOVE);
            graph.addEdge(clockPort1, clockPin1);
            graph.addEdge(clockPort2, clockPin2);
            clocks.add(clockPin1);
            clocks.add(clockPin2);
        }

        return new ConfigurableLogicBlock(graph, inputs, outputs, clocks);
    }

    /**
     * Gets switch block in which routing switches lay on the diagonal intersections and can be configured to bidirectionally
     * connect two distinct pairs of wires simultaneously.
     * @param wireCount The number of columns and rows of the routing switch block.
     * @param tShape    Whether the block is in a T-shape. If so, there exists no top connections.
     * @return The routing switch block.
     */
    public static Switchblock getSwitchBlock(int wireCount, boolean tShape) {
        HierarchyGraph graph = new HierarchyGraph();
        Switch mySwitch = Switch();
        if (tShape) {
            mySwitch.addOption(new IntConnection(0, 1), new IntConnection(1, 0));
            mySwitch.addOption(new IntConnection(0, 2), new IntConnection(2, 0));
            mySwitch.addOption(new IntConnection(1, 2), new IntConnection(2, 1));
        } else {
            mySwitch.addOption(new IntConnection(0, 1), new IntConnection(1, 0), new IntConnection(2, 3), new IntConnection(3, 2));
            mySwitch.addOption(new IntConnection(0, 2), new IntConnection(2, 0), new IntConnection(1, 3), new IntConnection(3, 1));
            mySwitch.addOption(new IntConnection(0, 3), new IntConnection(3, 0), new IntConnection(1, 2), new IntConnection(2, 1));
        } // 0 is left, 1 is right, 2 is bottom, 3 is top
        List<Vertex> leftPorts = new LinkedList<>();
        List<Vertex> rightPorts = new LinkedList<>();
        List<Vertex> topPorts = tShape ? null : new LinkedList<>();
        List<Vertex> bottomPorts = new LinkedList<>();
        for (int i = 0; i < wireCount; i++) {
            Vertex component = graph.addComponent(mySwitch.getHierarchyGraph(), "Switch");
            Vertex leftPort = graph.addPort(mySwitch.getByNumber(0), component);
            Vertex leftRemove = graph.addVertex(Label.REMOVE);
            graph.addEdge(leftPort, leftRemove);
            leftPorts.add(leftRemove);

            Vertex rightPort = graph.addPort(mySwitch.getByNumber(1), component);
            Vertex rightRemove = graph.addVertex(Label.REMOVE);
            graph.addEdge(rightPort, rightRemove);
            rightPorts.add(rightRemove);

            Vertex bottomPort = graph.addPort(mySwitch.getByNumber(2), component);
            Vertex bottomRemove = graph.addVertex(Label.REMOVE);
            graph.addEdge(bottomPort, bottomRemove);
            bottomPorts.add(bottomRemove);

            if (!tShape) {
                Vertex topPort = graph.addPort(mySwitch.getByNumber(3), component);
                Vertex topRemove = graph.addVertex(Label.REMOVE);
                graph.addEdge(topPort, topRemove);
                topPorts.add(topRemove);
            }
        }
        return new Switchblock(graph, leftPorts, rightPorts, topPorts, bottomPorts);
    }


    /**
     * A connection between two Integers (modeling conditional connections) in routing switches.
     */
    public static class IntConnection {

        /**
         * Where the current flows from.
         */
        public final int from;
        /**
         * Where the current flows to.
         */
        public final int to;

        /**
         * Instantiates a new IntConnection.
         *
         * @param from Where the current flows from.
         * @param to   Where the current flows to.
         */
        public IntConnection(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    /**
     * Utility class to store a connection between two Vertices.
     */
    public static class NodeConnection {

        /**
         * Vertex from which the connection flows.
         */
        public final Vertex to;
        /**
         * Vertex to which the connection flows.
         */
        public final Vertex from;

        /**
         * Instantiates a new connection between two Vertices.
         *
         * @param from Vertex from which the connection flows.
         * @param to   Vertex to which the connection flows.
         */
        public NodeConnection(Vertex from, Vertex to) {
            this.from = from;
            this.to = to;
        }
    }
}
