package data.graph;

import data.patterns.*;
import util.Util;

import java.util.*;

/**
 * Class that creates graph patterns for subcomponents.
 */
public final class Patterns {


    private static Map<Integer, MUX> muxMap = new HashMap<>();
    /**
     * Creates a MUX Hierarchygraph.
     * @param inCount The number of wires each input has (and thus the number of wires the output has).
     * @return A MUX Object containing the created Hierarchygraph.
     */
    public static MUX MUX(int inCount) {
        if (muxMap.containsKey(inCount)) {
            return muxMap.get(inCount);
        }
        HierarchyGraph res = new HierarchyGraph();
        Vertex mux1 = res.addVertex(Label.MUX);
        Vertex mux2 = res.addVertex(Label.MUX);
        List<Vertex> topInputs = new LinkedList<>();
        List<Vertex> bottomInputs = new LinkedList<>();
        List<Vertex> outputs = new LinkedList<>();
        for (int i = 0; i < inCount; i++) {
            Vertex in1 = res.addVertex(Label.IN);
            Vertex in2 = res.addVertex(Label.IN);
            Vertex out = res.addVertex(Label.OUT);
            topInputs.add(in1);
            bottomInputs.add(in2);
            outputs.add(out);
            res.addEdge(in1, mux1);
            res.addEdge(in1, out);
            res.addEdge(in2, mux2);
            res.addEdge(in2, out);
        }
        Vertex select = res.addVertex(Label.SELECT);
        res.addEdge(select, mux1);
        res.addEdge(select, mux2);
        MUX result = new MUX(res, topInputs, bottomInputs, outputs, select);
        muxMap.put(inCount, result);
        return result;
    }

    private static Map<Integer, Map<Integer, LUT>> LUTMap = new HashMap<>();
    /**
     * Creates a LUT Hierarchygraph.
     * @param inCount  The number of inputs of the LUT.
     * @param outCount The number of outputs of the LUT.
     * @return A LUT Object containing the created Hierarchygraph.
     */
    public static LUT LUT(int inCount, int outCount) {
        if (LUTMap.containsKey(inCount) && LUTMap.get(inCount).containsKey(outCount)) {
            return LUTMap.get(inCount).get(outCount);
        }
        HierarchyGraph res = new HierarchyGraph();
        Vertex mux1 = res.addVertex(Label.MUX, Label.LUT);
        Vertex mux2 = res.addVertex(Label.MUX, Label.LUT);
        List<Vertex> inputs = new LinkedList<>();
        List<Vertex> outputs = new LinkedList<>();
        for (int i = 0; i < inCount; i++) {
            Vertex in1 = res.addVertex(Label.IN, Label.SELECT);
            inputs.add(in1);
            res.addEdge(in1, mux1);
            res.addEdge(in1, mux2);
        }
        for (int i = 0; i < outCount; i++) {
            Vertex out = res.addVertex(Label.OUT);
            outputs.add(out);
            for (Vertex input : inputs) {
                res.addEdge(input, out);
            }
        }
        LUT result = new LeafLUT(res, inputs, outputs);
        LUTMap.putIfAbsent(inCount, new HashMap<>());
        LUTMap.get(inCount).put(outCount, result);
        return result;
    }

    private static Map<Integer, Map<Boolean, Map<Boolean, Register>>> registerMap = new HashMap<>();

    /**
     * Register register.
     *
     * @param wirecount   the wirecount
     * @param syncSet     the sync set
     * @param asyncSet    the async set
     * @param syncReset   the sync reset
     * @param asyncReset  the async reset
     * @param clockEnable the clock enable
     * @return the register
     */
    public static Register Register(int wirecount, boolean syncSet, boolean asyncSet, boolean syncReset, boolean asyncReset, boolean clockEnable) {
        if (registerMap.containsKey(wirecount) && registerMap.get(wirecount).containsKey(syncReset) && registerMap.get(wirecount).get(syncReset).containsKey(asyncReset)) {
            return registerMap.get(wirecount).get(syncReset).get(asyncReset);
        }
        HierarchyGraph res = new HierarchyGraph();
        List<Vertex> ins = new ArrayList<>(wirecount);
        List<Vertex> outs = new ArrayList<>(wirecount);
        Util.assertOrElse(syncSet || asyncReset, "A register must have some set vertex.");
        Vertex central = res.addVertex(Label.REGISTER);
        //Node set = syncReset ? res.addNode(Label.SYNC_SET) : null;

        for (int i = 0; i < wirecount; i++) {
            Vertex in = res.addVertex(Label.IN);
            Vertex out = res.addVertex(Label.OUT);
            ins.add(in);
            outs.add(out);
            res.addEdge(in, out);
            res.addEdge(in, central);
        }
        Vertex syncResetNode = null;
        Vertex asyncResetNode = null;
        Vertex syncSetNode = null;
        Vertex asyncSetNode = null;
        Vertex clockEnableNode = null;
        if (asyncReset) {
            asyncResetNode = res.addVertex(Label.ASYNC_RESET);
            res.addEdge(asyncResetNode, central);
        }
        if (syncReset) {
            syncResetNode = res.addVertex(Label.SYNC_RESET);
            res.addEdge(syncResetNode, central);
        }
        if (syncSet) {
            syncSetNode = res.addVertex(Label.SYNC_SET);
            res.addEdge(syncSetNode, central);
        }
        if (asyncSet) {
            clockEnableNode = res.addVertex(Label.CLOCK_ENABLE);
            res.addEdge(clockEnableNode, central);
        }
        if (clockEnable) {
            asyncSetNode = res.addVertex(Label.ASYNC_SET);
            res.addEdge(asyncSetNode, central);
        }
        Register result = new Register(res, ins, outs, syncSetNode, asyncSetNode, syncResetNode, asyncResetNode, clockEnableNode);
        registerMap.putIfAbsent(wirecount, new HashMap<>());
        registerMap.get(wirecount).putIfAbsent(syncReset, new HashMap<>());
        registerMap.get(wirecount).get(syncReset).putIfAbsent(asyncReset, result);
        return result;
    }

    /**
     * Add pins list.
     *
     * @param graph the graph
     * @param count the count
     * @return the list
     */
    public static List<Vertex> addPins(HierarchyGraph graph, int count) {
        List<Vertex> pins = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            pins.add(graph.addVertex(Label.PIN));
        }
        return pins;
    }

    /**
     * Switch switch.
     *
     * @return the switch
     */
    public static Switch Switch() {
        return new Switch();
    }


    /**
     * The type Int connection.
     */
    public static class IntConnection {

        /**
         * The From.
         */
        public final int from;
        /**
         * The To.
         */
        public final int to;

        /**
         * Instantiates a new Int connection.
         *
         * @param from the from
         * @param to   the to
         */
        public IntConnection(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    /**
     * The type Node connection.
     */
    public static class NodeConnection {

        /**
         * The To.
         */
        public final Vertex to;
        /**
         * The From.
         */
        public final Vertex from;

        /**
         * Instantiates a new Node connection.
         *
         * @param from the from
         * @param to   the to
         */
        public NodeConnection(Vertex from, Vertex to) {
            this.from = from;
            this.to = to;
        }
    }
}
