package data.graph;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import reader.Reader;

import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HierarchyGraph {

    private Set<Node> V = new HashSet<>();

    private Map<Node, Set<Node>> E = new HashMap<>();
    private Map<Node, HierarchyGraph> H = new HashMap<>();
    private Map<Node, Node> C = new HashMap<>();
    private Map<Label, Set<Node>> labelIndex = new HashMap<>();
    private boolean locked = false;

    private Map<HierarchyGraph, String> namesOfHierarchyGraphs = new HashMap<>();

    private List<Pair<String, Set<Node>>> subGraphLabeling = new LinkedList<>();

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
    }


    public void addEdge(Node a, Node b) {
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

    public void addPortMapping(Node higher, Node lower) {
        assert higher.getLabels().contains(Label.PORT);
        C.put(higher, lower);
    }


    private int subGraphCounter = 0;
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
        for (Pair<String, Set<Node>> entry : subGraphLabeling) {
            lines.add("subgraph cluster_" + subGraphCounter++ + " {");
            lines.add("label = \"" + entry.getOne() +"\"");
            for (Node node : entry.getTwo()) {
                lines.add(node.getID() + ";");
            }
            lines.add("}");
        }
    }

    private Map<HierarchyGraph, HierarchyGraph> flattenedCache = new HashMap<>();
    private HierarchyGraph getFlat(HierarchyGraph from) {
        if (flattenedCache.containsKey(from)) {
            return flattenedCache.get(from);
        } else {
            HierarchyGraph res = from.flatten();
            flattenedCache.put(from, res);
            return res;
        }
    }

    public HierarchyGraph flatten() {
        HierarchyGraph res = new HierarchyGraph();
        res.addNodes(V);
        res.addEdges(E);
        for (Map.Entry<Node, HierarchyGraph> hierarchies : H.entrySet()) { //For every subgraph
            Set<Node> ports = getPorts(hierarchies.getKey());
            res.removeNode(hierarchies.getKey());                          //remove component
            HierarchyGraph value = getFlat(hierarchies.getValue());        //get a flattened version
            Pair<HierarchyGraph, Map<Node, Node>> copy = value.deepCopy(); //make a copy of it

            for (Node port : ports) {
                res.replaceNode(port, copy.getTwo().get(C.get(port)));
            }

            res.addNodes(copy.getOne().getNodes());
            res.addEdges(copy.getOne().getEdges());

            String name = namesOfHierarchyGraphs.get(hierarchies.getValue());
            name = Reader.graphIds.getOrDefault(Paths.get(name), name);
            subGraphLabeling.add(Tuples.pair(name, copy.getOne().getNodes()));
        }
        //Util.assertConnected(res);
        res.subGraphLabeling = subGraphLabeling;
        return res;
    }

    private Set<Node> getPorts(Node component) {
        Set<Node> res = new HashSet<>(E.get(component));
        assert !res.retainAll(getNodesByLabel(Label.PORT));
        return res;
    }

    private Pair<HierarchyGraph, Map<Node, Node>> deepCopy() {
        Map<Node, Node> nodemap = new HashMap<>();
        HierarchyGraph res = new HierarchyGraph();
        for (Node replaced : V) {
            Node replacedBy = res.addNode(new HashSet<>(replaced.getLabels()));
            nodemap.put(replaced, replacedBy);
        }
        for (Map.Entry<Node, Set<Node>> entry : E.entrySet()) {
            for (Node target : entry.getValue()) {
                res.addEdge(nodemap.get(entry.getKey()), nodemap.get(target));
            }
        }
        Map<Node, Node> subCopyMapping = new HashMap<>();
        for (Map.Entry<Node, HierarchyGraph> entry : H.entrySet()) {
            Pair<HierarchyGraph, Map<Node, Node>> deepercopy = entry.getValue().deepCopy();
            subCopyMapping.putAll(deepercopy.getTwo());
            res.addComponent(deepercopy.getOne(), namesOfHierarchyGraphs.get(entry.getValue()));
        }
        for (Map.Entry<Node, Node> entry : C.entrySet()) {
            res.addPortMapping(nodemap.get(entry.getKey()), subCopyMapping.get(entry.getValue()));
        }
        assert V.size() == res.getNodes().size();
        for (Node i : V) {
            assert res.V.contains(nodemap.get(i));
        }
        assert E.size() == res.getEdges().size();
        for (Node i : E.keySet()) {
            assert res.E.get(nodemap.get(i)).size() == E.get(i).size();
            for (Node j : E.get(i)) {
                assert res.E.get(nodemap.get(i)).contains(nodemap.get(j));
            }
        }
        assert C.size() == res.getPortMapping().size();
        for (Node i : C.keySet()) {
            assert res.C.containsKey(nodemap.get(i)) && !res.C.get(nodemap.get(i)).equals(C.get(i));
        }
        assert H.size() == res.getHierarchy().size();
        assert nodemap.size() == V.size();
        return Tuples.pair(res, nodemap);
    }

    private void replaceNode(Node key, Node value) {
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
    }

    private void removeNode(Node component) {
        V.remove(component);
        for (Node neighbour : E.getOrDefault(component, Collections.emptySet())) {
            E.get(neighbour).remove(component);
        }
        E.remove(component);

    }

    public Map<Node, Set<Node>> getEdges() {
        return Collections.unmodifiableMap(E);
    }

    public Set<Node> getNodes() {
        return Collections.unmodifiableSet(V);
    }

    private void addEdges(Map<Node, Set<Node>> e) {
        for (Map.Entry<Node, Set<Node>> entry : e.entrySet()) {
            if (E.containsKey(entry.getKey())) {
                E.get(entry.getKey()).addAll(entry.getValue());
            } else {
                E.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
        }
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
        Node res = addNode(Label.COMPONENT);
        addHierarchy(res, hierarchyGraph, name);
        return res;
    }

    public Node addPort(Node linkTarget, Node component) {
        Node res = addNode(Label.PORT);
        addPortMapping(res, linkTarget);
        if (component != null) {
            addEdge(res, component);
        }
        return res;
    }

    public void shuffleIdentifiers() {
        shuffleIdentifiers(System.currentTimeMillis());
    }

    public void shuffleIdentifiers(long seed) {
        List<Node> from = new ArrayList<>(V);
        List<Node> to = new ArrayList<>(from);
        Collections.shuffle(to, new Random(seed));
        Map<Node, Node> mapping = new HashMap<>();
        for (int i = 0; i < from.size(); i++) {
            mapping.put(from.get(i), to.get(i));
        }

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
        }
        E = E_new;
        H = H_new;
        C = C_new;
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
}
