package data.patterns;

import data.graph.HierarchyGraph;
import data.graph.Vertex;

import java.util.Collections;
import java.util.List;

public class Switchblock {

    public final HierarchyGraph graph;
    public final List<Vertex> left;
    public final List<Vertex> right;
    public final List<Vertex> bottom;
    public final List<Vertex> top;

    public Switchblock(HierarchyGraph graph, List<Vertex> left, List<Vertex> right, List<Vertex> top, List<Vertex> bottom) {
        this.graph = graph;
        this.graph.lock();
        this.left = Collections.unmodifiableList(left);
        this.right = Collections.unmodifiableList(right);
        this.top = top == null ? null : Collections.unmodifiableList(top);
        this.bottom = Collections.unmodifiableList(bottom);
    }
}
