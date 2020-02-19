package data;


import data.graph.HierarchyGraph;
import data.graph.Node;
import data.graph.Path;

public class PartialMapping {

    private HierarchyGraph source;
    private HierarchyGraph target;

    private Path[] mapping;

    public PartialMapping(HierarchyGraph source, HierarchyGraph target) {
        mapping = new Path[source.vertexCount()];
    }

    public void add(Node source, Path target) {
        if (mapping[source.getID()] != null) {
            throw new RuntimeException("Assigning node that was already assigned");
        }
        mapping[source.getID()] = target;
    }

    public void remove(Node source) {
        mapping[source.getID()] = null;
    }
}
