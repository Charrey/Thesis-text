package charrey.data;


import charrey.graph.HierarchyGraph;
import charrey.graph.Vertex;
import charrey.graph.Path;

import java.util.Map;

/**
 * The type Partial mapping.
 */
public class PartialMapping {

    private HierarchyGraph source;
    private HierarchyGraph target;

    private Path[] mapping;

    /**
     * Instantiates a new Partial mapping.
     *
     * @param source the source
     * @param target the target
     */
    public PartialMapping(HierarchyGraph source, HierarchyGraph target) {
        mapping = new Path[source.getVertices().size()];
    }

    /**
     * Instantiates a new Partial mapping.
     *
     * @param top   the top
     * @param graph the graph
     * @param map   the map
     */
    public PartialMapping(HierarchyGraph top, HierarchyGraph graph, Map<Vertex, Vertex> map) {
    }

    /**
     * Add.
     *
     * @param source the source
     * @param target the target
     */
    public void add(Vertex source, Path target) {
        if (mapping[source.getID()] != null) {
            throw new RuntimeException("Assigning vertex that was already assigned");
        }
        mapping[source.getID()] = target;
    }

    /**
     * Remove.
     *
     * @param source the source
     */
    public void remove(Vertex source) {
        mapping[source.getID()] = null;
    }
}
