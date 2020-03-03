package data.graph;

import data.patterns.*;
import util.Util;

import java.util.*;

public final class Patterns {

    private static Map<Integer, MUX> muxMap = new HashMap<>();
    public static MUX MUX(int inCount) {
        if (muxMap.containsKey(inCount)) {
            return muxMap.get(inCount);
        }
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
        MUX result = new MUX(res, topInputs, bottomInputs, outputs, select);
        muxMap.put(inCount, result);
        return result;
    }

    private static Map<Integer, Map<Integer, LUT>> LUTMap = new HashMap<>();
    public static LUT LUT(int inCount, int outCount) {
        if (LUTMap.containsKey(inCount) && LUTMap.get(inCount).containsKey(outCount)) {
            return LUTMap.get(inCount).get(outCount);
        }
        HierarchyGraph res = new HierarchyGraph();
        Node mux1 = res.addNode(Label.MUX, Label.LUT);
        Node mux2 = res.addNode(Label.MUX, Label.LUT);
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
        LUT result = new LeafLUT(res, inputs, outputs);
        LUTMap.putIfAbsent(inCount, new HashMap<>());
        LUTMap.get(inCount).put(outCount, result);
        return result;
    }

    private static Map<Integer, Map<Boolean, Map<Boolean, Register>>> registerMap = new HashMap<>();
    public static Register Register(int wirecount, boolean syncSet, boolean asyncSet, boolean syncReset, boolean asyncReset, boolean clockEnable) {
        if (registerMap.containsKey(wirecount) && registerMap.get(wirecount).containsKey(syncReset) && registerMap.get(wirecount).get(syncReset).containsKey(asyncReset)) {
            return registerMap.get(wirecount).get(syncReset).get(asyncReset);
        }
        HierarchyGraph res = new HierarchyGraph();
        List<Node> ins = new ArrayList<>(wirecount);
        List<Node> outs = new ArrayList<>(wirecount);
        Util.assertOrElse(syncSet || asyncReset, "A register must have some set node.");
        Node central = res.addNode(Label.REGISTER);
        //Node set = syncReset ? res.addNode(Label.SYNC_SET) : null;

        for (int i = 0; i < wirecount; i++) {
            Node in = res.addNode(Label.IN);
            Node out = res.addNode(Label.OUT);
            ins.add(in);
            outs.add(out);
            res.addEdge(in, out);
            res.addEdge(in, central);
        }
        Node syncResetNode = null;
        Node asyncResetNode = null;
        Node syncSetNode = null;
        Node asyncSetNode = null;
        Node clockEnableNode = null;
        if (asyncReset) {
            asyncResetNode = res.addNode(Label.ASYNC_RESET);
            res.addEdge(asyncResetNode, central);
        }
        if (syncReset) {
            syncResetNode = res.addNode(Label.SYNC_RESET);
            res.addEdge(syncResetNode, central);
        }
        if (syncSet) {
            syncSetNode = res.addNode(Label.SYNC_SET);
            res.addEdge(syncSetNode, central);
        }
        if (asyncSet) {
            clockEnableNode = res.addNode(Label.CLOCK_ENABLE);
            res.addEdge(clockEnableNode, central);
        }
        if (clockEnable) {
            asyncSetNode = res.addNode(Label.ASYNC_SET);
            res.addEdge(asyncSetNode, central);
        }
        Register result = new Register(res, ins, outs, syncSetNode, asyncSetNode, syncResetNode, asyncResetNode, clockEnableNode);
        registerMap.putIfAbsent(wirecount, new HashMap<>());
        registerMap.get(wirecount).putIfAbsent(syncReset, new HashMap<>());
        registerMap.get(wirecount).get(syncReset).putIfAbsent(asyncReset, result);
        return result;
    }

    public static List<Node> addPins(HierarchyGraph graph, int count) {
        List<Node> pins = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            pins.add(graph.addNode(Label.PIN));
        }
        return pins;
    }

    public static Switch Switch() {
        return new Switch();
    }


    public static class IntConnection {

        public final int from;
        public final int to;

        public IntConnection(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    public static class NodeConnection {

        public final Node to;
        public final Node from;

        public NodeConnection(Node from, Node to) {
            this.from = from;
            this.to = to;
        }
    }
}
