package data.graph;

import util.BiMap;
import util.Util;

import java.util.*;
import java.util.stream.Collectors;

public class HierarchyGraph {

    private Set<Node> V = new HashSet<>();

    private Map<Node, Set<Node>> E = new HashMap<>();
    private Map<Node, HierarchyGraph> H = new HashMap<>();
    private Map<Node, Node> C = new HashMap<>();
    private Map<Label, Set<Node>> labelIndex = new HashMap<>();
    private boolean locked = false;

    private Map<HierarchyGraph, String> namesOfHierarchyGraphs = new HashMap<>();

    public List<Subgraph> subGraphLabeling = new LinkedList<>();
    private int subGraphCounter = 0;



    public void lock() {
        if (locked) {
            return;
        }
        V = Collections.unmodifiableSet(V);
        H = Collections.unmodifiableMap(H);
        C = Collections.unmodifiableMap(C);
        for (Map.Entry<Node, Set<Node>> entry : new HashSet<>(E.entrySet())) {
            E.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        }
        E = Collections.unmodifiableMap(E);
        for (Node node : V) {
            node.lock();
        }
        for (Map.Entry<Node, HierarchyGraph> entry : H.entrySet()) {
            entry.getValue().lock();
        }
        locked = true;
        assert H.entrySet().stream().allMatch(x -> getPorts(x.getKey()).stream().allMatch(y -> x.getValue().getNodes().contains(C.get(y))));
    }


    public void addEdge(Node a, Node b) {
        assert a != null;
        assert b != null;
        assert V.contains(a);
        assert V.contains(b);
        E.computeIfAbsent(a, x -> new HashSet<>());
        E.computeIfAbsent(b, x -> new HashSet<>());
        E.get(a).add(b);
        E.get(b).add(a);
    }


     @Override
     public String toString() {
        return (locked ? "LOCKED" : "") + "(" + V + "," + E + "," + H + "," + C + ")";
     }

    public void addHierarchy(Node component, HierarchyGraph graph, String name) {
        H.put(component, graph);
        namesOfHierarchyGraphs.put(graph, name);
    }

    private void addPortMapping(Node higher, Node lower) {
        assert higher!= null;
        assert lower != null;
        assert higher.getLabels().contains(Label.PORT);
        C.put(higher, lower);
    }


    public String toDOT(boolean scramble) {
        StringBuilder sb = new StringBuilder("graph G {\n");
        List<String> lines = new LinkedList<>();
        for (Node n : V) {
            lines.add(n.getDOT());
        }
        Set<Node> seen = new HashSet<>();
        for (Node n : E.keySet()) {
            for (Node m : E.get(n)) {
                if (seen.contains(m)) {
                    continue;
                }
                lines.add(n.getID() + "--" + m.getID());
            }
            seen.add(n);
        }
        if (scramble) {
            Collections.shuffle(lines);
        }
        addSubGraphs(lines);
        for (String line : lines) {
            sb.append(line).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    private void addSubGraphs(List<String> lines) {
        for (Subgraph subgraph : subGraphLabeling) {
            if (V.containsAll(subgraph.getNodes())) {
                lines.add("subgraph cluster_" + subGraphCounter++ + " {");
                lines.add("label = \"" + subgraph.getName() + "\"");
                for (Node node : subgraph.getNodes()) {
                    lines.add(node.getID() + ";");
                }
                lines.add("}");
            }
        }
    }

//    public static HierarchyGraph getFlat(HierarchyGraph from) {
//        if (flattenedCache.containsKey(from)) {
//            System.out.println("Cache hit!");
//            return flattenedCache.get(from);
//        } else {
//            HierarchyGraph res = from.flatten();
//            flattenedCache.put(from, res);
//            return res;
//        }
//    }

    public CopyInfo flatten() {
        HierarchyGraph res = new HierarchyGraph();
        BiMap<Node, Node> nodemap = new BiMap<>();
        Set<HierarchyGraph> toAdd = new HashSet<>();
        Map<Node, Node> CToUniqueHierarchyGraphs = new HashMap<>();

        for (Map.Entry<Node, HierarchyGraph> subgraph : H.entrySet()) {
            CopyInfo flattened = subgraph.getValue().flatten();
            toAdd.add(flattened.graph);
            for (Node port : E.get(subgraph.getKey())) {
                CToUniqueHierarchyGraphs.put(port, flattened.getMap().get(C.get(port)));
            }
            res.subGraphLabeling.add(new Subgraph(namesOfHierarchyGraphs.get(subgraph.getValue()), flattened.graph.V));
        }
        for (Node node : V) {
            nodemap.put(node, new Node(node.getLabels()));
        }
        res.V = V.stream().filter(node -> !(node.getLabels().contains(Label.COMPONENT))).map(nodemap::get).collect(Collectors.toSet());
        res.V.addAll(toAdd.stream().map(graph -> graph.V).reduce(new HashSet<>(), (nodes, nodes2) -> {
            nodes.addAll(nodes2);
            return nodes;
        }));
        res.E = E.entrySet()
                .stream()
                .filter(x -> res.V.contains(nodemap.get(x.getKey())))
                .collect(Collectors.toMap(
                        x -> nodemap.get(x.getKey()),
                        y -> y.getValue()
                                .stream()
                                .filter(x -> res.V.contains(nodemap.get(x)))
                                .map(nodemap::get)
                                .collect(Collectors.toSet())));
        res.rebuildLabelIndex();
        //Util.checkConsistent(res);
        for (Map<Node, Set<Node>> edgeSets : toAdd.stream().map(graph -> graph.E).collect(Collectors.toSet())) {
            for (Map.Entry<Node, Set<Node>> entry : edgeSets.entrySet()) {
                res.E.putIfAbsent(entry.getKey(), new HashSet<>());
                res.E.get(entry.getKey()).addAll(new HashSet<>(entry.getValue()));
            }
        }
        res.rebuildLabelIndex();
        //Util.checkConsistent(res);
        for (Map.Entry<Node, Node> portMapping : C.entrySet()) {
            Node port = portMapping.getKey();
            res.addEdge(nodemap.get(port), CToUniqueHierarchyGraphs.get(port));
        }

        while (res.getNodesByLabel(Label.PORT).stream().anyMatch(x -> CToUniqueHierarchyGraphs.containsKey(nodemap.getByValue(x).iterator().next()))) {
            Node example = res.getNodesByLabel(Label.PORT).stream().filter(x -> CToUniqueHierarchyGraphs.containsKey(nodemap.getByValue(x).iterator().next())).findAny().get();
            for (Node one : res.E.get(example)) {
                for (Node two : res.E.get(example)) {
                    if (two.getID() > one.getID()) {
                        res.addEdge(one, two);
                    }
                }
            }
            res.removeNode(example);
        }
        res.rebuildLabelIndex();
        res.subGraphCounter = 0;
        return new CopyInfo(res, nodemap.getToMap());
    }

    private void rebuildLabelIndex() {
        labelIndex = new HashMap<>();
        for (Node node : V) {
            for (Label label : node.getLabels()) {
                labelIndex.putIfAbsent(label, new HashSet<>());
                labelIndex.get(label).add(node);
            }
        }
    }

    private Set<Node> getPorts(Node component) {
        if (!E.containsKey(component)) {
            return Collections.emptySet();
        }
        Set<Node> res = new HashSet<>(E.get(component));
        Util.checkConsistent(this);
        assert !res.retainAll(getNodesByLabel(Label.PORT));
        assert res.stream().noneMatch(node1 -> E.get(node1).stream().anyMatch(node2 -> node2.getLabels().contains(Label.COMPONENT) && node2 != component));
        assert res.stream().allMatch(x -> C.containsKey(x));
        return res;
    }

    public CopyInfo deepCopy() {
        Util.checkConsistent(this);
        Map<Node, Node> nodemap = new HashMap<>();
        Map<HierarchyGraph, HierarchyGraph> graphmap = new HashMap<>();
        HierarchyGraph res = new HierarchyGraph();
        for (HierarchyGraph subgraph : H.values()) {
            CopyInfo copyInfo = subgraph.deepCopy();
            nodemap.putAll(copyInfo.getMap());
            graphmap.put(subgraph, copyInfo.graph);
        }
        for (Node node : V) {
           nodemap.put(node, new Node(node.getLabels()));
        }
        res.V = V.stream().map(nodemap::get).collect(Collectors.toSet());
        res.E = E.entrySet().stream()
                .map(nodeSetEntry -> Map.entry(nodemap.get(nodeSetEntry.getKey()), nodeSetEntry.getValue().stream().map(nodemap::get).collect(Collectors.toSet())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        res.H = H.entrySet().stream()
                .map(nodeSetEntry -> Map.entry(nodemap.get(nodeSetEntry.getKey()), graphmap.get(nodeSetEntry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        res.C = C.entrySet().stream()
                .collect(Collectors.toMap(nodeNodeEntry -> nodemap.get(nodeNodeEntry.getKey()), nodeNodeEntry -> nodemap.get(nodeNodeEntry.getValue())));
        res.labelIndex = labelIndex.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, labelSetEntry -> labelSetEntry.getValue().stream().map(nodemap::get).collect(Collectors.toSet())));
        res.namesOfHierarchyGraphs = namesOfHierarchyGraphs.entrySet().stream()
                .collect(Collectors.toMap(hierarchyGraphStringEntry -> graphmap.get(hierarchyGraphStringEntry.getKey()), Map.Entry::getValue));
        res.subGraphLabeling = subGraphLabeling.stream().map(subgraph ->
                new Subgraph(subgraph.name, subgraph.nodes.stream().map(nodemap::get).collect(Collectors.toSet())))
                .collect(Collectors.toList());
        res.subGraphCounter = subGraphCounter;
        Util.checkConsistent(res);
        return new CopyInfo(res, nodemap);
    }

    private Node getComponent(Node key) {
        assert key.getLabels().contains(Label.PORT);
        Set<Node> candidates = new HashSet<>(E.get(key));
        candidates.retainAll(getNodesByLabel(Label.COMPONENT));
        assert candidates.size() == 1;
        return candidates.iterator().next();
    }

    private void replaceNode(Node key, Node value) {
        Util.checkConsistent(this);
        assert key != null;
        assert value != null;
        V.remove(key);
        for (Label label : key.getLabels()) {
            labelIndex.get(label).remove(key);
        }
        V.add(value);
        for (Node neighbour : E.get(key)) {
            E.get(neighbour).remove(key);
            E.get(neighbour).add(value);
        }
        E.computeIfAbsent(value, x -> new HashSet<>());
        E.get(value).addAll(E.get(key));
        E.remove(key);
        Util.checkConsistent(this);
    }

    public void removeNode(Node node) {
        Util.checkConsistent(this);
        V.remove(node);
        for (Node neighbour : E.getOrDefault(node, Collections.emptySet())) {
            E.get(neighbour).remove(node);
        }
        for (Label label : node.getLabels()) {
            labelIndex.get(label).remove(node);
        }
        E.remove(node);
        Util.checkConsistent(this);
    }

    public Map<Node, Set<Node>> getEdges() {
        return Collections.unmodifiableMap(E);
    }

    public Set<Node> getNodes() {
        return Collections.unmodifiableSet(V);
    }

    private void addEdges(Map<Node, Set<Node>> e) {
        Util.checkConsistent(this);
        for (Map.Entry<Node, Set<Node>> entry : e.entrySet()) {
            if (E.containsKey(entry.getKey())) {
                E.get(entry.getKey()).addAll(entry.getValue());
            } else {
                E.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
        }
        Util.checkConsistent(this);
    }

    private void addNodes(Set<Node> v) {
        v.forEach(this::addNode);
    }

    public Node addNode(Label... labels) {
        return addNode(new Node(labels));
    }

    public Node addNode(Set<Label> labels) {
        return addNode(new Node(labels));
    }


    private Node addNode(Node node) {
        assert !V.contains(node);
        E.put(node, new HashSet<>());
        V.add(node);
        for (Label label : node.getLabels()) {
            labelIndex.computeIfAbsent(label, x -> new HashSet<>());
            labelIndex.get(label).add(node);
        }
        return node;
    }


    public int vertexCount() {
        return V.size();
    }

    public Node addComponent(HierarchyGraph hierarchyGraph, String name) {
        Util.checkConsistent(this);
        Node res = addNode(Label.COMPONENT);
        addHierarchy(res, hierarchyGraph, name);
        Util.checkConsistent(this);
        return res;
    }

    public Node addPort(Node linkTarget, Node component) {
        Util.checkConsistent(this);
        Node res = addNode(Label.PORT);
        assert H.containsKey(component);
        addPortMapping(res, linkTarget);
        if (component != null) {
            addEdge(res, component);
        }
        Util.checkConsistent(this);
        return res;
    }

    public void shuffleIdentifiers() {
        shuffleIdentifiers(System.currentTimeMillis());
    }

    public void shuffleIdentifiers(long seed) {
        Util.checkConsistent(this);
        List<Node> from = new ArrayList<>(V);
        List<Node> to = new ArrayList<>(from);
        Collections.shuffle(to, new Random(seed));
        Map<Node, Node> mapping = new HashMap<>();
        for (int i = 0; i < from.size(); i++) {
            mapping.put(from.get(i), to.get(i));
        }
        labelIndex = new HashMap<>();
        Map<Node, Set<Node>> E_new = new HashMap<>();
        Map<Node, HierarchyGraph> H_new = new HashMap<>();
        Map<Node, Node> C_new = new HashMap<>();
        for (Map.Entry<Node, Set<Node>> entry : E.entrySet()) {
            E_new.put(mapping.get(entry.getKey()), entry.getValue().stream().map(mapping::get).collect(Collectors.toSet()));
        }
        for (Map.Entry<Node, HierarchyGraph> entry : H.entrySet()) {
            H_new.put(mapping.get(entry.getKey()), entry.getValue());
        }
        for (Map.Entry<Node, Node> entry : C.entrySet()) {
            C_new.put(mapping.get(entry.getKey()), entry.getValue());
        }
        Map<Node, Set<Label>> newLabels = new HashMap<>();
        for (Node n : from) {
            newLabels.put(mapping.get(n), n.getLabels());
        }
        for (Node n : to) {
            n.setLabels(newLabels.get(n));
            for (Label label : newLabels.get(n)) {
                labelIndex.putIfAbsent(label, new HashSet<>());
                labelIndex.get(label).add(n);
            }
        }
        E = E_new;
        H = H_new;
        C = C_new;
        Util.checkConsistent(this);
    }

    public Map<Node, HierarchyGraph> getHierarchy() {
        return Collections.unmodifiableMap(H);
    }

    public Set<Node> getNodesByLabel(Label label) {
        return labelIndex.getOrDefault(label, Collections.emptySet());
    }

    public Map<Node, Node> getPortMapping() {
        return Collections.unmodifiableMap(C);
    }

    public Map<HierarchyGraph, String> getNamesOfHierarchyGraphs() {
        return namesOfHierarchyGraphs;
    }

    public class CopyInfo {
        private final HierarchyGraph graph;
        private final Map<Node, Node> map;

        public CopyInfo(HierarchyGraph graph, Map<Node, Node> map) {
            this.graph = graph;
            this.map = map;
        }

        public Map<Node, Node> getMap() {
            return map;
        }

        public HierarchyGraph getGraph() {
            return graph;
        }
    }

    private class Subgraph {
        private final Set<Node> nodes;
        private final String name;

        public Subgraph(String name, Set<Node> nodes) {
            this.name = name;
            this.nodes = nodes;
        }

        public String getName() {
            return name;
        }

        public Set<Node> getNodes() {
            return nodes;
        }
    }
}
