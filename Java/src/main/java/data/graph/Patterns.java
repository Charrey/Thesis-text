package data.graph;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Patterns {

    public static MUX MUX(int inCount) {
        HierarchyGraph res = new HierarchyGraph();
        Node mux1 = res.addNode(Label.MUX);
        Node mux2 = res.addNode(Label.MUX);
        List<Node> topInputs = new LinkedList<>();
        List<Node> bottomInputs = new LinkedList<>();
        List<Node> outputs = new LinkedList<>();
        for (int i = 0; i < inCount; i++) {
            Node in1 = res.addNode(Label.IN);
            Node in2 = res.addNode(Label.IN);
            Node out = res.addNode(Label.OUT);
            topInputs.add(in1);
            bottomInputs.add(in2);
            outputs.add(out);
            res.addEdge(in1, mux1);
            res.addEdge(in1, out);
            res.addEdge(in2, mux2);
            res.addEdge(in2, out);
        }
        Node select = res.addNode(Label.SELECT);
        res.addEdge(select, mux1);
        res.addEdge(select, mux2);
        return new MUX(res, topInputs, bottomInputs, outputs, select);
    }

    public static LUT LUT(int inCount, int outCount) {
        HierarchyGraph res = new HierarchyGraph();
        Node mux1 = res.addNode(Label.MUX, Label.LUT);
        Node mux2 = res.addNode(Label.MUX, Label.LUT);
        List<Node> inputs = new LinkedList<>();
        List<Node> outputs = new LinkedList<>();
        for (int i = 0; i < inCount; i++) {
            Node in1 = res.addNode(Label.IN, Label.SELECT);
            inputs.add(in1);
            res.addEdge(in1, mux1);
            res.addEdge(in1, mux2);
        }
        for (int i = 0; i < outCount; i++) {
            Node out = res.addNode(Label.OUT);
            outputs.add(out);
            for (Node input : inputs) {
                res.addEdge(input, out);
            }
        }
        return new LUT(res, inputs, outputs);
    }

    public static Register Register(int wirecount, boolean syncReset, boolean asyncReset) {
        HierarchyGraph res = new HierarchyGraph();
        List<Node> ins = new ArrayList<>(wirecount);
        List<Node> outs = new ArrayList<>(wirecount);
        Node set = res.addNode(Label.SET);
        for (int i = 0; i < wirecount; i++) {
            Node in = res.addNode(Label.IN);
            Node out = res.addNode(Label.OUT);
            ins.add(in);
            outs.add(out);
            res.addEdge(in, out);
            res.addEdge(in, set);
        }
        Node syncResetNode = null;
        Node asyncResetNode = null;
        if (asyncReset) {
            asyncResetNode = res.addNode(Label.ASYNC_RESET);
            res.addEdge(asyncResetNode, set);
        }
        if (syncReset) {
            syncResetNode = res.addNode(Label.SYNC_RESET);
            res.addEdge(syncResetNode, set);
        }
        return new Patterns.Register(res, ins, outs, set, syncResetNode, asyncResetNode);
    }

    public static List<Node> addPins(HierarchyGraph graph, int count) {
        List<Node> pins = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            pins.add(graph.addNode(Label.PIN));
        }
        return pins;
    }

    public static List<Node> addPorts(HierarchyGraph graph, int count) {
        List<Node> pins = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            pins.add(graph.addNode(Label.PORT));
        }
        return pins;
    }

    public static Switch Switch() {
        return new Switch();
    }



    public static class MUX {
        public final HierarchyGraph hierarchyGraph;
        public final List<Node> in1;
        public final List<Node> in2;
        public final Node select;
        public final List<Node> out;

        private MUX(HierarchyGraph hg, List<Node> in1, List<Node> in2, List<Node> out, Node select) {
            this.hierarchyGraph = hg;
            this.hierarchyGraph.lock();
            this.in1 = Collections.unmodifiableList(in1);
            this.in2 = Collections.unmodifiableList(in2);
            this.out = Collections.unmodifiableList(out);
            this.select = select;
        }
    }

    public static class LUT {
        public final HierarchyGraph hierarchyGraph;
        public final List<Node> outputs;
        public final List<Node> inputs;

        public LUT(HierarchyGraph hierarchyGraph, List<Node> inputs, List<Node> outputs) {
            this.hierarchyGraph = hierarchyGraph;
            this.hierarchyGraph.lock();
            this.inputs = Collections.unmodifiableList(inputs);
            this.outputs = Collections.unmodifiableList(outputs);
        }
    }

    public static class Register {
        public final HierarchyGraph hierarchyGraph;
        public final List<Node> inputs;
        public final List<Node> outputs;
        public final Node set;
        public final Node syncReset;
        public final Node asyncReset;

        public Register(HierarchyGraph hierarchyGraph, List<Node> inputs, List<Node> outputs, Node set, Node syncReset, Node asyncReset) {
            this.hierarchyGraph = hierarchyGraph;
            this.hierarchyGraph.lock();
            this.inputs = Collections.unmodifiableList(inputs);
            this.outputs = Collections.unmodifiableList(outputs);
            this.set = set;
            this.syncReset = syncReset;
            this.asyncReset = asyncReset;
        }
    }

    public static class Switch {
        private Map<Integer, Node> ports = new HashMap<>();
        private HierarchyGraph graph = new HierarchyGraph();

        private Map<Node, Map<Node, Node>> connections = new HashMap<>();

        public void addOption(Pair<Integer, Integer>... connections) {
            Set<Pair<Node, Node>> option = Arrays.stream(connections)
                    .map((Function<Pair<Integer, Integer>, Pair<Node, Node>>) x -> Tuples.twin(ports.computeIfAbsent(x.getOne(), y -> graph.addNode(Label.SWITCH)), ports.computeIfAbsent(x.getTwo(), y -> graph.addNode(Label.SWITCH))))
                    .collect(Collectors.toSet());
            Node optionNode = graph.addNode(Label.OPTION);
            for (Pair<Node, Node> connection : option) {
                this.connections.computeIfAbsent(connection.getOne(), x -> new HashMap<>());
                Node flowTo;
                if (!this.connections.get(connection.getOne()).containsKey(connection.getTwo())) {
                    Node flowFrom = graph.addNode(Label.FLOW_FROM);
                    flowTo = graph.addNode(Label.FLOW_TO);
                    graph.addEdge(connection.getOne(), flowFrom);
                    graph.addEdge(flowFrom, flowTo);
                    graph.addEdge(flowTo, connection.getTwo());
                    this.connections.computeIfAbsent(connection.getOne(), x -> new HashMap<>());
                    this.connections.get(connection.getOne()).put(connection.getTwo(), flowTo);
                } else {
                    flowTo = this.connections.get(connection.getOne()).get(connection.getTwo());
                }
                graph.addEdge(optionNode, flowTo);
            }
        }

        public HierarchyGraph getHierarchyGraph() {
            graph.lock();
            return graph;
        }

        public Node getByNumber(int number) {
            return ports.get(number);
        }
    }
}
