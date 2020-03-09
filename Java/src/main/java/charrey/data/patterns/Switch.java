package charrey.data.patterns;

import charrey.graph.HierarchyGraph;
import charrey.graph.Label;
import charrey.graph.Vertex;
import charrey.graph.generator.SubgraphGenerator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Switch {
    private Map<Integer, Vertex> ports = new HashMap<>();
    private HierarchyGraph graph = new HierarchyGraph();

    private Map<Vertex, Map<Vertex, Vertex>> connections = new HashMap<>();

    public void addOption(SubgraphGenerator.IntConnection... connections) {
        Set<SubgraphGenerator.NodeConnection> option = Arrays.stream(connections)
                .map((Function<SubgraphGenerator.IntConnection, SubgraphGenerator.NodeConnection>) x -> new SubgraphGenerator.NodeConnection(ports.computeIfAbsent(x.from, y -> graph.addVertex(Label.SWITCH)), ports.computeIfAbsent(x.to, y -> graph.addVertex(Label.SWITCH))))
                .collect(Collectors.toSet());
        Vertex optionNode = graph.addVertex(Label.OPTION);
        for (SubgraphGenerator.NodeConnection connection : option) {
            this.connections.computeIfAbsent(connection.from, x -> new HashMap<>());
            Vertex flowTo;
            if (!this.connections.get(connection.from).containsKey(connection.to)) {
                Vertex flowFrom = graph.addVertex(Label.FLOW_FROM);
                flowTo = graph.addVertex(Label.FLOW_TO);
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

    public Vertex getByNumber(int number) {
        return ports.get(number);
    }
}