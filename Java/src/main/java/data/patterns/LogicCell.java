package data.patterns;

import data.graph.HierarchyGraph;
import data.graph.Vertex;

import java.util.Collections;
import java.util.List;

public class LogicCell {
    public final HierarchyGraph graph;
    public final List<Vertex> inputs;
    public final List<Vertex> outputs;
    public final Vertex clockPort;

    public LogicCell(HierarchyGraph graph, List<Vertex> inputs, List<Vertex> outputs, Vertex clockPort) {
        this.graph = graph;
        graph.lock();
        this.inputs = Collections.unmodifiableList(inputs);
        this.outputs = Collections.unmodifiableList(outputs);
        this.clockPort = clockPort;
    }
}
