package charrey.iso;

import charrey.data.PartialMapping;
import charrey.graph.HierarchyGraph;
import charrey.graph.Vertex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class State {

    PartialMapping mapping;
    Map<Vertex, Set<Vertex>> domains = new HashMap<>();

    public static State unconstrained(HierarchyGraph virtual, HierarchyGraph concrete) {
        State state = new State();
        virtual.getVertices().forEach(x -> state.domains.put(x, new HashSet<>()));
        virtual.getVertices().forEach(x -> state.domains.get(x).addAll(concrete.getVertices()));
        return state;
    }
}
