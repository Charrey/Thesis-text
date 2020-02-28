package data.patterns;

import data.graph.HierarchyGraph;
import data.graph.Node;

import java.util.List;

public interface LUT {

    HierarchyGraph getHierarchyGraph();
    List<Node> getOutputs();
    List<Node> getInputs();
}
