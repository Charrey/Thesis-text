package data.patterns;

import data.graph.HierarchyGraph;
import data.graph.Node;

import java.util.Collections;
import java.util.List;

public class LeafLUT implements LUT {
    public final HierarchyGraph hierarchyGraph;
    public final List<Node> outputs;
    public final List<Node> inputs;

    public LeafLUT(HierarchyGraph hierarchyGraph, List<Node> inputs, List<Node> outputs) {
        this.hierarchyGraph = hierarchyGraph;
        this.hierarchyGraph.lock();

        assert hierarchyGraph.getNodes().containsAll(inputs);
        assert hierarchyGraph.getNodes().containsAll(outputs);
        this.inputs = Collections.unmodifiableList(inputs);
        this.outputs = Collections.unmodifiableList(outputs);
    }

    @Override
    public HierarchyGraph getHierarchyGraph() {
        return hierarchyGraph;
    }

    @Override
    public List<Node> getOutputs() {
        return outputs;
    }

    @Override
    public List<Node> getInputs() {
        return inputs;
    }
}