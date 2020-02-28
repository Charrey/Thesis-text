package data.patterns;

import data.graph.HierarchyGraph;
import data.graph.Node;

import java.util.Collections;
import java.util.List;

public class Register {
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