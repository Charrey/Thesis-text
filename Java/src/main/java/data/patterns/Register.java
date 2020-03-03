package data.patterns;

import data.graph.HierarchyGraph;
import data.graph.Vertex;

import java.util.Collections;
import java.util.List;

public class Register {
    public final HierarchyGraph hierarchyGraph;
    public final List<Vertex> inputs;
    public final List<Vertex> outputs;
    public final Vertex syncReset;
    public final Vertex asyncReset;
    public final Vertex syncSet;
    public final Vertex asyncSet;
    public final Vertex clockEnable;

    public Register(HierarchyGraph hierarchyGraph, List<Vertex> inputs, List<Vertex> outputs, Vertex syncSet, Vertex asyncSet, Vertex syncReset, Vertex asyncReset, Vertex clockEnable) {
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