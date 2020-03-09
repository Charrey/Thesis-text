package charrey.data.patterns;

import charrey.graph.HierarchyGraph;
import charrey.graph.Vertex;

import java.util.List;

public interface LUT {

    HierarchyGraph getHierarchyGraph();
    List<Vertex> getOutputs();
    List<Vertex> getInputs();
}
