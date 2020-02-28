package data.patterns;

import data.graph.HierarchyGraph;
import data.graph.Label;
import data.graph.Node;
import data.graph.Patterns;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Switch {
    private Map<Integer, Node> ports = new HashMap<>();
    private HierarchyGraph graph = new HierarchyGraph();

    private Map<Node, Map<Node, Node>> connections = new HashMap<>();

    public void addOption(Patterns.IntConnection... connections) {
        Set<Patterns.NodeConnection> option = Arrays.stream(connections)
                .map((Function<Patterns.IntConnection, Patterns.NodeConnection>) x -> new Patterns.NodeConnection(ports.computeIfAbsent(x.from, y -> graph.addNode(Label.SWITCH)), ports.computeIfAbsent(x.to, y -> graph.addNode(Label.SWITCH))))
                .collect(Collectors.toSet());
        Node optionNode = graph.addNode(Label.OPTION);
        for (Patterns.NodeConnection connection : option) {
            this.connections.computeIfAbsent(connection.from, x -> new HashMap<>());
            Node flowTo;
            if (!this.connections.get(connection.from).containsKey(connection.to)) {
                Node flowFrom = graph.addNode(Label.FLOW_FROM);
                flowTo = graph.addNode(Label.FLOW_TO);
                graph.addEdge(connection.from, flowFrom);
                graph.addEdge(flowFrom, flowTo);
                graph.addEdge(flowTo, connection.to);
                this.connections.computeIfAbsent(connection.from, x -> new HashMap<>());
                this.connections.get(connection.from).put(connection.to, flowTo);
            } else {
                flowTo = this.connections.get(connection.from).get(connection.to);
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