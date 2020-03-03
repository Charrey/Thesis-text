package data.patterns;

import data.graph.HierarchyGraph;
import data.graph.Node;

import java.util.Collections;
import java.util.List;

public class Register {
    public final HierarchyGraph hierarchyGraph;
    public final List<Node> inputs;
    public final List<Node> outputs;
    public final Node syncReset;
    public final Node asyncReset;
    public final Node syncSet;
    public final Node asyncSet;
    public final Node clockEnable;

    public Register(HierarchyGraph hierarchyGraph, List<Node> inputs, List<Node> outputs, Node syncSet, Node asyncSet, Node syncReset, Node asyncReset, Node clockEnable) {
        this.hierarchyGraph = hierarchyGraph;
        this.hierarchyGraph.lock();
        this.inputs = Collections.unmodifiableList(inputs);
        this.outputs = Collections.unmodifiableList(outputs);
        this.syncSet = syncSet;
        this.asyncSet = asyncSet;
        this.syncReset = syncReset;
        this.asyncReset = asyncReset;
        this.clockEnable = clockEnable;
    }
}