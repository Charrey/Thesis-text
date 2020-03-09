package charrey.data.patterns;

import charrey.graph.HierarchyGraph;
import charrey.graph.Vertex;
import charrey.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigurableLogicBlock {

    public final HierarchyGraph graph;
    public final List<List<Vertex>> inputs;
    public final List<List<Vertex>> outputs;
    public final Set<Vertex> clocks;

    public ConfigurableLogicBlock(HierarchyGraph graph, List<List<Vertex>> inputs, List<List<Vertex>> outputs, Set<Vertex> clocks) {
        this.graph = graph;
        this.graph.lock();
        this.outputs = Collections.unmodifiableList(outputs.stream().map(Collections::unmodifiableList).collect(Collectors.toList()));
        this.inputs = Collections.unmodifiableList(inputs.stream().map(Collections::unmodifiableList).collect(Collectors.toList()));
        this.clocks = Collections.unmodifiableSet(clocks);
    }

    public List<Vertex> getAllInputs() {
        return Util.concat(inputs);
    }
}
