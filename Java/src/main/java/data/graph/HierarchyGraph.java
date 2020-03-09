package data.graph;

import util.BiMap;
import util.Util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A hierarchical Graph that may contain inner graphs.
 */
public class HierarchyGraph {

    private Set<Vertex> V = new HashSet<>();

    private Map<Vertex, Set<Vertex>> E = new HashMap<>();
    private Map<Vertex, HierarchyGraph> H = new HashMap<>();
    private Map<Vertex, Vertex> C = new HashMap<>();
    private Map<Label, Set<Vertex>> labelIndex = new HashMap<>();
    private boolean locked = false;

    public static Map<HierarchyGraph, String> namesOfHierarchyGraphs = new HashMap<>();

    /**
     * A list of vertex sets that have names. This is used in visual representations of Hierarchygraphs to provide names to subgraphs.
     */
    public List<Subgraph> subGraphLabeling = new LinkedList<>();
    private int subGraphCounter = 0;


    /**
     * Permanently prohibit modification of this Hierarchygraph.
     */
    public void lock() {
        if (locked) {
            return;
        }
        V = Collections.unmodifiableSet(V);
        H = Collections.unmodifiableMap(H);
        C = Collections.unmodifiableMap(C);
        for (Map.Entry<Vertex, Set<Vertex>> entry : new HashSet<>(E.entrySet())) {
            E.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        }
        E = Collections.unmodifiableMap(E);
        for (Vertex vertex : V) {
            vertex.lock();
        }
        for (Map.Entry<Vertex, HierarchyGraph> entry : H.entrySet()) {
            entry.getValue().lock();
        }
        labelIndex.replaceAll((k, v) -> Collections.unmodifiableSet(labelIndex.get(k)));
        labelIndex = Collections.unmodifiableMap(labelIndex);
        locked = true;
        assert H.entrySet().stream().allMatch(x -> getPorts(x.getKey()).stream().allMatch(y -> x.getValue().getVertices().contains(C.get(y))));
    }


    /**
     * Adds an undirected edge between two existing vertices.
     *
     * @param a One vertex to be connected
     * @param b The other vertex to be connected
     */
    public void addEdge(Vertex a, Vertex b) {
        Util.assertOrElse(a != null, "Attempting to add an edge starting from NULL.");
        Util.assertOrElse(b != null, "Attempting to add an edge going to NULL.");
        Util.assertOrElse(V.contains(a), "Attempting to add an edge starting from a vertex that is not in V.");
        Util.assertOrElse(V.contains(b), "Attempting to add an edge going to a vertex that is not in V.");
        E.computeIfAbsent(a, x -> new HashSet<>());
        E.computeIfAbsent(b, x -> new HashSet<>());
        E.get(a).add(b);
        E.get(b).add(a);
    }


    @Override
    public String toString() {
        return (locked ? "LOCKED" : "") + "(" + V + "," + E + "," + H + "," + C + ")";
    }

    private void addHierarchy(Vertex component, HierarchyGraph graph, String name) {
        H.put(component, graph);
        namesOfHierarchyGraphs.put(graph, name);
    }

    private void addPortMapping(Vertex higher, Vertex lower) {
        assert higher != null;
        assert lower != null;
        assert higher.getLabels().contains(Label.PORT);
        C.put(higher, lower);
    }


    /**
     * Returns a String in DOT format giving an accurate visualisation of this Hierarchygraph.
     *
     * @param scramble whether to scramble the lines of the resulting String. Scrambling
     *                 may influence the way graphs are displayed.
     * @return A valid graph in DOT-format representative of this Hierarchygraph.
     */
    public String toDOT(boolean scramble) {
        StringBuilder sb = new StringBuilder("graph G {\n");
        List<String> lines = new LinkedList<>();
        for (Vertex n : V) {
            lines.add(n.getDOT());
        }
        Set<Vertex> seen = new HashSet<>();
        for (Vertex n : E.keySet()) {
            for (Vertex m : E.get(n)) {
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
            if (V.containsAll(subgraph.getVertices())) {
                lines.add("subgraph cluster_" + subGraphCounter++ + " {");
                lines.add("label = \"" + subgraph.getName() + "\"");
                for (Vertex vertex : subgraph.getVertices()) {
                    lines.add(vertex.getID() + ";");
                }
                lines.add("}");
            }
        }
    }

    private static Map<HierarchyGraph, Map<Integer, HierarchyGraph>> flatCache = new HashMap<>();
    public static HierarchyGraph getFlat(HierarchyGraph from, int flattenDepth, boolean copy) {
        if (flatCache.containsKey(from) && flatCache.get(from).containsKey(flattenDepth)) {
            return copy ? flatCache.get(from).get(flattenDepth).deepCopy().getGraph() : flatCache.get(from).get(flattenDepth);
        } else {
            HierarchyGraph res = from.flatten(flattenDepth).getGraph();
            flatCache.putIfAbsent(from, new HashMap<>());
            flatCache.get(from).put(flattenDepth, res);
            return res;
        }
    }

    /**
     * Returns a Hierarchygraph in which each subgraph is merged in to the top-level one. This graph contains the same
     * information as thís one, but has no more hierarchy.
     *
     * @return Both a flattened graph of this one, and a mapping that maps vertices from this graph to their equivalent in
     * the resulting graph.
     * @param flattenDepth
     */
    private CopyInfo flatten(int flattenDepth) {
        if (flattenDepth == 0) {
            CopyInfo copy = deepCopy();
            for (Map.Entry<Vertex, HierarchyGraph> unresolvedHierarchy : copy.getGraph().getHierarchy().entrySet()) {
                copy.getGraph().subGraphLabeling.add(new Subgraph(namesOfHierarchyGraphs.get(unresolvedHierarchy.getValue()), Set.of(unresolvedHierarchy.getKey())));
            }
            return copy;
        }
        HierarchyGraph res = new HierarchyGraph();
        BiMap<Vertex, Vertex> vertexMap = new BiMap<>();
        Set<HierarchyGraph> toAdd = new HashSet<>();
        Map<Vertex, Vertex> CToUniqueHierarchyGraphs = new HashMap<>();

        for (Map.Entry<Vertex, HierarchyGraph> hierarchies : H.entrySet()) {
            CopyInfo flattened = hierarchies.getValue().flatten(flattenDepth - 1);
            toAdd.add(flattened.graph);
            for (Vertex port : E.get(hierarchies.getKey())) {
                CToUniqueHierarchyGraphs.put(port, flattened.getMap().get(C.get(port)));
            }
            vertexMap.putAll(flattened.getMap());
            res.subGraphLabeling.add(new Subgraph(namesOfHierarchyGraphs.get(hierarchies.getValue()), flattened.graph.V));
        }
        for (Vertex vertex : V) {
            vertexMap.put(vertex, new Vertex(vertex.getLabels()));
        }
        res.V = V.stream().filter(vertex -> !(vertex.getLabels().contains(Label.COMPONENT))).map(vertexMap::get).collect(Collectors.toSet());
        res.V.addAll(toAdd.stream().map(graph -> graph.V).reduce(new HashSet<>(), (vertices, vertices2) -> {
            vertices.addAll(vertices2);
            return vertices;
        }));
        res.E = E.entrySet()
                .stream()
                .filter(x -> res.V.contains(vertexMap.get(x.getKey())))
                .collect(Collectors.toMap(
                        x -> vertexMap.get(x.getKey()),
                        y -> y.getValue()
                                .stream()
                                .filter(x -> res.V.contains(vertexMap.get(x)))
                                .map(vertexMap::get)
                                .collect(Collectors.toSet())));
        res.rebuildLabelIndex();
        //Util.checkConsistent(res);
        for (Map<Vertex, Set<Vertex>> edgeSets : toAdd.stream().map(graph -> graph.E).collect(Collectors.toSet())) {
            for (Map.Entry<Vertex, Set<Vertex>> entry : edgeSets.entrySet()) {
                res.E.putIfAbsent(entry.getKey(), new HashSet<>());
                res.E.get(entry.getKey()).addAll(new HashSet<>(entry.getValue()));
            }
        }
        res.rebuildLabelIndex();
        //Util.checkConsistent(res);
        for (Map.Entry<Vertex, Vertex> portMapping : C.entrySet()) {
            Vertex port = portMapping.getKey();
            res.addEdge(vertexMap.get(port), CToUniqueHierarchyGraphs.get(port));
        }

        try {
            while (res.getVerticesByLabel(Label.PORT).stream().anyMatch(x -> vertexMap.containsValue(x) && CToUniqueHierarchyGraphs.containsKey(vertexMap.getByValue(x).iterator().next()))) {
                Vertex example = res.getVerticesByLabel(Label.PORT).stream().filter(x -> vertexMap.containsValue(x) && CToUniqueHierarchyGraphs.containsKey(vertexMap.getByValue(x).iterator().next())).findAny().get();
                for (Vertex one : res.E.get(example)) {
                    for (Vertex two : res.E.get(example)) {
                        if (two.getID() > one.getID()) {
                            res.addEdge(one, two);
                        }
                    }
                }
                res.removeVertex(example);
            }
        } catch (NoSuchElementException e) {}
        res.rebuildLabelIndex();
        res.subGraphCounter = 0;
        vertexMap.removeValues(x -> !res.getVertices().contains(x) && H.values().stream().noneMatch(y -> y.getVertices().contains(x)));
        return new CopyInfo(res, vertexMap.getToMap());
    }

    private void rebuildLabelIndex() {
        labelIndex = new HashMap<>();
        for (Vertex vertex : V) {
            for (Label label : vertex.getLabels()) {
                labelIndex.putIfAbsent(label, new HashSet<>());
                labelIndex.get(label).add(vertex);
            }
        }
    }

    private Set<Vertex> getPorts(Vertex component) {
        if (!E.containsKey(component)) {
            return Collections.emptySet();
        }
        Set<Vertex> res = new HashSet<>(E.get(component));
        Util.checkConsistent(this);
        assert !res.retainAll(getVerticesByLabel(Label.PORT));
        assert res.stream().noneMatch(vertex -> E.get(vertex).stream().anyMatch(otherVertex -> otherVertex.getLabels().contains(Label.COMPONENT) && otherVertex != component));
        assert res.stream().allMatch(x -> C.containsKey(x));
        return res;
    }

    /**
     * Returns a structurally identical but independent copy of this graph as a new Object.
     *
     * @return Both the copy of this graph and a mapping that maps vertices from this graph to their copy in the resulting
     * graph.
     */
    public CopyInfo deepCopy() {
        Util.checkConsistent(this);
        Map<Vertex, Vertex> vertexMap = new HashMap<>();
        Map<HierarchyGraph, HierarchyGraph> graphmap = new HashMap<>();
        HierarchyGraph res = new HierarchyGraph();
        for (HierarchyGraph subgraph : H.values()) {
            CopyInfo copyInfo = subgraph.deepCopy();
            vertexMap.putAll(copyInfo.getMap());
            graphmap.put(subgraph, copyInfo.graph);
        }
        for (Vertex vertex : V) {
            vertexMap.put(vertex, new Vertex(vertex.getLabels()));
        }
        res.V = V.stream().map(vertexMap::get).collect(Collectors.toSet());
        res.E = E.entrySet().stream()
                .map(entry -> Map.entry(vertexMap.get(entry.getKey()), entry.getValue().stream().map(vertexMap::get).collect(Collectors.toSet())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        res.H = H.entrySet().stream()
                .map(entry -> Map.entry(vertexMap.get(entry.getKey()), graphmap.get(entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        res.C = C.entrySet().stream()
                .collect(Collectors.toMap(entry -> vertexMap.get(entry.getKey()), entry -> vertexMap.get(entry.getValue())));
        res.labelIndex = labelIndex.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().map(vertexMap::get).collect(Collectors.toSet())));
        namesOfHierarchyGraphs.putAll(namesOfHierarchyGraphs.entrySet().stream().filter(x -> graphmap.containsKey(x.getKey()))
                .collect(Collectors.toMap(entry -> graphmap.get(entry.getKey()), Map.Entry::getValue)));
        res.subGraphLabeling = subGraphLabeling.stream().map(subgraph ->
                new Subgraph(subgraph.name, subgraph.vertices.stream().map(vertexMap::get).collect(Collectors.toSet())))
                .collect(Collectors.toList());
        res.subGraphCounter = subGraphCounter;
        Util.checkConsistent(res);
        return new CopyInfo(res, vertexMap);
    }


    /**
     * Removes a vertex from this Hierarchygraph.
     *
     * @param vertex The vertex to be removed.
     */
    public void removeVertex(Vertex vertex) {
        V.remove(vertex);
        for (Vertex neighbour : E.getOrDefault(vertex, Collections.emptySet())) {
            E.get(neighbour).remove(vertex);
        }
        for (Label label : vertex.getLabels()) {
            labelIndex.get(label).remove(vertex);
        }
        E.remove(vertex);
        C.remove(vertex);
        H.remove(vertex);
    }

    /**
     * Gets the edges of the top hierarchy of this Hierarchygraph.
     * @return A map that maps vertices to their neighbours.
     */
    public Map<Vertex, Set<Vertex>> getEdges() {
        return Collections.unmodifiableMap(E);
    }

    /**
     * Gets the vertices of the top hierarchy of this Hierarchygraph.
     * @return Set of vertices.
     */
    public Set<Vertex> getVertices() {
        return Collections.unmodifiableSet(V);
    }

    /**
     * Add a vertex to the top hierarchy of this Hierarchygraph.
     * @param labels The labels this vertex has.
     * @return The vertex that was added.
     */
    public Vertex addVertex(Label... labels) {
        return addVertex(new Vertex(labels));
    }

    /**
     * Add a vertex to the top hierarchy of this Hierarchygraph.
     * @param labels The labels this vertex has.
     * @return The vertex that was added.
     */
    public Vertex addVertex(Set<Label> labels) {
        return addVertex(new Vertex(labels));
    }

    private Vertex addVertex(Vertex vertex) {
        assert !V.contains(vertex);
        E.put(vertex, new HashSet<>());
        V.add(vertex);
        for (Label label : vertex.getLabels()) {
            labelIndex.computeIfAbsent(label, x -> new HashSet<>());
            labelIndex.get(label).add(vertex);
        }
        return vertex;
    }


    /**
     * Adds hierarchy to this Hierarchygraph by embedding another Hierarchygraph into a node in this graph.
     * @param hierarchyGraph The Hierarchygraph to be embedded in this one.
     * @param name           Name of this hierarchygraph (for visualisation purposes only)
     * @return The vertex that represents the subhierarchygraph.
     */
    public Vertex addComponent(HierarchyGraph hierarchyGraph, String name) {
        Vertex res = addVertex(Label.COMPONENT);
        addHierarchy(res, hierarchyGraph, name);
        return res;
    }


    /**
     * Adds a port to a vertex of lower hierarchy.
     * @param linkTarget the vertex to link this port to.
     * @param component  the component vertex that represents the hierarchygraph that contains the link target.
     * @return The port vertex that was added.
     */
    public Vertex addPort(Vertex linkTarget, Vertex component) {
        Util.assertOrElse(linkTarget != null, "Cannot add port to a NULL link target.");
        Util.assertOrElse(!linkTarget.getLabels().contains(Label.PORT), "Attempting to link a port to a vertex that is a port itself. This is a vertex that may disappear and is thus not allowed!");
        Vertex res = addVertex(Label.PORT);
        assert H.containsKey(component);
        addPortMapping(res, linkTarget);
        if (component != null) {
            addEdge(res, component);
        }
        return res;
    }

    /**
     * Replaces nodes in this graph by random other nodes, leaving the structure of this hierarchygraph intact.
     * This may be used to ensure isomorphism algorithms do not rely on internal identifiers.
     */
    public void shuffleIdentifiers() {
        shuffleIdentifiers(System.currentTimeMillis());
    }

    /**
     * Replaces nodes in this graph by random other nodes, leaving the structure of this hierarchygraph intact.
     * This may be used to ensure isomorphism algorithms do not rely on internal identifiers.
     * @param seed The seed used for randomisation.
     */
    public void shuffleIdentifiers(long seed) {
        List<Vertex> from = new ArrayList<>(V);
        List<Vertex> to = new ArrayList<>(from);
        Collections.shuffle(to, new Random(seed));
        Map<Vertex, Vertex> mapping = new HashMap<>();
        for (int i = 0; i < from.size(); i++) {
            mapping.put(from.get(i), to.get(i));
        }


        labelIndex = new HashMap<>();
        Map<Vertex, Set<Vertex>> E_new = new HashMap<>();
        Map<Vertex, HierarchyGraph> H_new = new HashMap<>();
        Map<Vertex, Vertex> C_new = new HashMap<>();
        for (Map.Entry<Vertex, Set<Vertex>> entry : E.entrySet()) {
            E_new.put(mapping.get(entry.getKey()), entry.getValue().stream().map(mapping::get).collect(Collectors.toSet()));
        }
        for (Map.Entry<Vertex, HierarchyGraph> entry : H.entrySet()) {
            H_new.put(mapping.get(entry.getKey()), entry.getValue());
        }
        for (Map.Entry<Vertex, Vertex> entry : C.entrySet()) {
            C_new.put(mapping.get(entry.getKey()), entry.getValue());
        }
        Map<Vertex, Set<Label>> newLabels = new HashMap<>();
        for (Vertex n : from) {
            newLabels.put(mapping.get(n), n.getLabels());
        }
        for (Vertex n : to) {
            n.setLabels(newLabels.get(n));
            for (Label label : newLabels.get(n)) {
                labelIndex.putIfAbsent(label, new HashSet<>());
                labelIndex.get(label).add(n);
            }
        }
        E = E_new;
        H = H_new;
        C = C_new;
    }

    /**
     * Returns a mapping describing which nodes describe which hierarchical subcomponents.
     * @return A mapping where the keys are vertices describing subcomponents, and the values are the subcomponents being described.
     */
    public Map<Vertex, HierarchyGraph> getHierarchy() {
        return Collections.unmodifiableMap(H);
    }

    /**
     * Gets all vertices that have at least some given label
     * @param label Label that each vertex should have,
     * @return All vertices with that label.
     */
    public Set<Vertex> getVerticesByLabel(Label label) {
        return Collections.unmodifiableSet(labelIndex.getOrDefault(label, Collections.emptySet()));
    }

    /**
     * Returns a mapping that connects vertices to vertices of lower hierarchy.
     * @return A mapping that connects vertices to vertices of lower hierarchy.
     */
    public Map<Vertex, Vertex> getPortMapping() {
        return Collections.unmodifiableMap(C);
    }

   // /**
    // * Gives a Map that maps hierarchygraphs to their names (for storage purposes)
    // * @return the Hierarchygraph-to-name map.
    // */
    //public Map<HierarchyGraph, String> getNamesOfHierarchyGraphs() {
    //    return namesOfHierarchyGraphs;
    //}

    /**
     * A class that contains both a new version of a Hierarchygraph via some process, and a mapping that describes
     * which old vertices have been mapped to which new vertices.
     */
    public static class CopyInfo {
        private final HierarchyGraph graph;
        private final Map<Vertex, Vertex> map;

        /**
         * Instantiates a new CopyInfo with the provided parameters.
         *
         * @param graph Hierarchygraph contained by this Object.
         * @param map   Mapping contains by this Object.
         */
        public CopyInfo(HierarchyGraph graph, Map<Vertex, Vertex> map) {
            this.graph = graph;
            this.map = map;
        }

        /**
         * Returns the map of this Object.
         * @return the map.
         */
        public Map<Vertex, Vertex> getMap() {
            return map;
        }

        /**
         * Returns the hierarchygraph of this Object.
         * @return the hierarchygraph
         */
        public HierarchyGraph getGraph() {
            return graph;
        }
    }

    private static class Subgraph {
        private final Set<Vertex> vertices;
        private final String name;

        private Subgraph(String name, Set<Vertex> vertices) {
            this.name = name;
            this.vertices = vertices;
        }

        private String getName() {
            return name;
        }

        private Set<Vertex> getVertices() {
            return vertices;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HierarchyGraph graph = (HierarchyGraph) o;
        return subGraphCounter == graph.subGraphCounter &&
                V.equals(graph.V) &&
                E.equals(graph.E) &&
                H.equals(graph.H) &&
                C.equals(graph.C) &&
                labelIndex.equals(graph.labelIndex) &&
                namesOfHierarchyGraphs.equals(graph.namesOfHierarchyGraphs) &&
                subGraphLabeling.equals(graph.subGraphLabeling);
    }

    @Override
    public int hashCode() {
        return Objects.hash(V, E, H, C, labelIndex, subGraphLabeling, subGraphCounter);
    }
}
