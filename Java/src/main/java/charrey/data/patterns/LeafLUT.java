package charrey.data.patterns;

import charrey.graph.HierarchyGraph;
import charrey.graph.Vertex;

import java.util.Collections;
import java.util.List;

public class LeafLUT implements LUT {
    public final HierarchyGraph hierarchyGraph;
    public final List<Vertex> outputs;
    public final List<Vertex> inputs;

    public LeafLUT(HierarchyGraph hierarchyGraph, List<Vertex> inputs, List<Vertex> outputs) {
        this.hierarchyGraph = hierarchyGraph;
        this.hierarchyGraph.lock();

        assert hierarchyGraph.getVertices().containsAll(inputs);
        assert hierarchyGraph.getVertices().containsAll(outputs);
        this.inputs = Collections.unmodifiableList(inputs);
        this.outputs = Collections.unmodifiableList(outputs);
    }

    @Override
    public HierarchyGraph getHierarchyGraph() {
        return hierarchyGraph;
    }

    @Override
    public List<Vertex> getOutputs() {
        return outputs;
    }

    @Override
    public List<Vertex> getInputs() {
        return inputs;
    }
}