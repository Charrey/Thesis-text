package data;


import data.graph.HierarchyGraph;
import data.graph.Vertex;
import data.graph.Path;

public class PartialMapping {

    private HierarchyGraph source;
    private HierarchyGraph target;

    private Path[] mapping;

    public PartialMapping(HierarchyGraph source, HierarchyGraph target) {
        mapping = new Path[source.getVertices().size()];
    }

    public void add(Vertex source, Path target) {
        if (mapping[source.getID()] != null) {
            throw new RuntimeException("Assigning vertex that was already assigned");
        }
        mapping[source.getID()] = target;
    }

    public void remove(Vertex source) {
        mapping[source.getID()] = null;
    }
}
