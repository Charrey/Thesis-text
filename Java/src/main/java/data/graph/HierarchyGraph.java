package data.graph;

import org.eclipse.collections.impl.list.Interval;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HierarchyGraph {

    private Set<Node> V = new HashSet<>();

    private Map<Node, Set<Node>> E = new HashMap<>();

    private Map<Node, HierarchyGraph> H = new HashMap<>();
    private Map<Node, Node> C = new HashMap<>();

    public void lock() {
        V = Collections.unmodifiableSet(V);
        E = Collections.unmodifiableMap(E);
        H = Collections.unmodifiableMap(H);
        C = Collections.unmodifiableMap(C);
    }


    public void addEdge(Node a, Node b) {
        E.computeIfAbsent(a, x -> new HashSet<>());
        E.computeIfAbsent(b, x -> new HashSet<>());
        E.get(a).add(b);
        E.get(b).add(a);
    }

    public Node addNode(Label... label) {
        Node n = new Node(label);
        V.add(n);
        return n;
    }

    public Node addNode(Set<Label> label) {
        Node n = new Node(label);
        V.add(n);
        return n;
    }

     @Override
     public String toString() {
        return "(" + V + "," + E + "," + H + "," + C + ")";
     }

    public void addHierarchy(Node component, HierarchyGraph graph) {
        H.put(component, graph);
    }

    public void addPortMapping(Node higher, Node lower) {
        C.put(higher, lower);
    }

    public String toDOT() {
        StringBuilder sb = new StringBuilder("graph G {\n");
        for (Node n : V) {
            sb.append(n.getDOT() + "\n");
        }
        Set<Node> seen = new HashSet<>();
        for (Node n : E.keySet()) {
            for (Node m : E.get(n)) {
                if (seen.contains(m)) {
                    continue;
                }
                sb.append(n.getID()).append("--").append(m.getID()).append("\n");
            }
            seen.add(n);
        }
        sb.append("}");
        return sb.toString();
    }

    public HierarchyGraph flatten() {
        HierarchyGraph res = new HierarchyGraph();
        res.addNodes(V);
        res.addEdges(E);
        for (Map.Entry<Node, HierarchyGraph> hierarchies : H.entrySet()) {
            Node component = hierarchies.getKey();
            res.removeNode(component);
            HierarchyGraph value = hierarchies.getValue().flatten();
            res.addNodes(value.getNodes());
            res.addEdges(value.getEdges());
        }
        for (Map.Entry<Node, Node> links : C.entrySet()) {
            System.out.println("Replacing " + links.getKey() + " by " + links.getValue());
            res.replaceNode(links.getKey(), links.getValue());
        }
        return res;
    }

    private void replaceNode(Node key, Node value) {
        V.remove(key);
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
        for (Node neighbour : E.get(component)) {
            E.get(neighbour).remove(component);
        }
        E.remove(component);

    }

    private Map<Node, Set<Node>> getEdges() {
        return E;
    }

    private Set<Node> getNodes() {
        return V;
    }

    private void addEdges(Map<Node, Set<Node>> e) {
        for (Map.Entry<Node, Set<Node>> entry : e.entrySet()) {
            if (E.containsKey(entry.getKey())) {
                E.get(entry.getKey()).addAll(entry.getValue());
            } else {
                E.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void addNodes(Set<Node> v) {
        V.addAll(v);
    }


    public int vertexCount() {
        return V.size();
    }

    public Node addComponent(HierarchyGraph hierarchyGraph) {
        Node res = addNode(Label.COMPONENT);
        addHierarchy(res, hierarchyGraph);
        return res;
    }

    public Node addPort(Node linkTarget) {
        Node res = addNode(Label.PORT);
        addPortMapping(res, linkTarget);
        return res;
    }

    public void shuffleIdentifiers(long seed) {
        List<Node> to = new ArrayList<>(V);
        Collections.shuffle(to, new Random(seed));
        Map<Node, Node> mapping = new HashMap<>();
        int counter = 0;
        for (Node v : V) {
            mapping.put(v, to.get(counter++));
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
        for (Node n : V) {
            newLabels.put(mapping.get(n), n.getLabels());
        }
        for (Node n : V) {
            n.setLabels(newLabels.get(n));
        }
        E = E_new;
        H = H_new;
        C = C_new;

    }
}
