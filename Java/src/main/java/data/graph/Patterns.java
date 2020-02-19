package data.graph;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Patterns {

    public static MUX MUX(int inCount) {
        HierarchyGraph res = new HierarchyGraph();
        Node mux1 = res.addNode(Label.MUX);
        Node mux2 = res.addNode(Label.MUX);
        List<Node> topInputs = new LinkedList<>();
        List<Node> bottomInputs = new LinkedList<>();
        List<Node> outputs = new LinkedList<>();
        for (int i = 0; i < inCount; i++) {
            Node in1 = res.addNode(Label.IN);
            Node in2 = res.addNode(Label.IN);
            Node out = res.addNode(Label.OUT);
            topInputs.add(in1);
            bottomInputs.add(in2);
            outputs.add(out);
            res.addEdge(in1, mux1);
            res.addEdge(in1, out);
            res.addEdge(in2, mux2);
            res.addEdge(in2, out);
        }
        Node select = res.addNode(Label.SELECT);
        res.addEdge(select, mux1);
        res.addEdge(select, mux2);
        return new MUX(res, topInputs, bottomInputs, outputs, select);
    }

    public static LUT LUT(int inCount, int outCount) {
        HierarchyGraph res = new HierarchyGraph();
        Node mux1 = res.addNode(Label.MUX);
        Node mux2 = res.addNode(Label.MUX);
        List<Node> inputs = new LinkedList<>();
        List<Node> outputs = new LinkedList<>();
        for (int i = 0; i < inCount; i++) {
            Node in1 = res.addNode(Label.IN, Label.SELECT);
            inputs.add(in1);
            res.addEdge(in1, mux1);
            res.addEdge(in1, mux2);
        }
        for (int i = 0; i < outCount; i++) {
            Node out = res.addNode(Label.OUT);
            outputs.add(out);
            for (Node input : inputs) {
                res.addEdge(input, out);
            }
        }
        return new LUT(res, inputs, outputs);
    }

    public static class MUX {
        public final HierarchyGraph hierarchyGraph;
        public final List<Node> in1;
        public final List<Node> in2;
        public final Node select;
        public final List<Node> out;

        private MUX(HierarchyGraph hg, List<Node> in1, List<Node> in2, List<Node> out, Node select) {
            this.hierarchyGraph = hg;
            this.hierarchyGraph.lock();
            this.in1 = Collections.unmodifiableList(in1);
            this.in2 = Collections.unmodifiableList(in2);
            this.out = Collections.unmodifiableList(out);
            this.select = select;
        }
    }

    private static class LUT {
        public final HierarchyGraph hierarchyGraph;
        public final List<Node> outputs;
        public final List<Node> inputs;

        public LUT(HierarchyGraph hierarchyGraph, List<Node> inputs, List<Node> outputs) {
            this.hierarchyGraph = hierarchyGraph;
            this.hierarchyGraph.lock();
            this.inputs = Collections.unmodifiableList(inputs);
            this.outputs = Collections.unmodifiableList(outputs);
        }
    }
}
