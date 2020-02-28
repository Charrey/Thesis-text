package data.patterns;

import data.graph.HierarchyGraph;
import data.graph.Node;

import java.util.Collections;
import java.util.List;

public class MUX {
    public final HierarchyGraph hierarchyGraph;
    public final List<Node> in1;
    public final List<Node> in2;
    public final Node select;
    public final List<Node> out;

    public MUX(HierarchyGraph hg, List<Node> in1, List<Node> in2, List<Node> out, Node select) {
        this.hierarchyGraph = hg;
        this.hierarchyGraph.lock();
        this.in1 = Collections.unmodifiableList(in1);
        this.in2 = Collections.unmodifiableList(in2);
        this.out = Collections.unmodifiableList(out);
        this.select = select;
    }
}