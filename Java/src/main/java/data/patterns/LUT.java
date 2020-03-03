package data.patterns;

import data.graph.HierarchyGraph;
import data.graph.Vertex;

import java.util.List;

public interface LUT {

    HierarchyGraph getHierarchyGraph();
    List<Vertex> getOutputs();
    List<Vertex> getInputs();
}
