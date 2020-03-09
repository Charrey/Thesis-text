package charrey.data.patterns;

import charrey.graph.HierarchyGraph;
import charrey.graph.Vertex;

import java.util.Collections;
import java.util.List;

public class MUX {
    public final HierarchyGraph hierarchyGraph;
    public final List<Vertex> in1;
    public final List<Vertex> in2;
    public final Vertex select;
    public final List<Vertex> out;

    public MUX(HierarchyGraph hg, List<Vertex> in1, List<Vertex> in2, List<Vertex> out, Vertex select) {
        this.hierarchyGraph = hg;
        this.hierarchyGraph.lock();
        this.in1 = Collections.unmodifiableList(in1);
        this.in2 = Collections.unmodifiableList(in2);
        this.out = Collections.unmodifiableList(out);
        this.select = select;
    }
}