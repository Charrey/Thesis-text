package charrey.reader;

import charrey.graph.HierarchyGraph;
import charrey.graph.Label;
import charrey.graph.Vertex;
import charrey.exceptions.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import charrey.util.BiMap;
import charrey.util.Labels;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Class that can read Hierarchygraphs from collections of files.
 */
public class Reader {

    /**
     * Instantiates a new Reader. Required for each file.
     */
    public Reader() {
        this(new HashMap<>());
        cache = new HashMap<>();
    }

    private Map<String, Vertex> globalXMLIdentifiers;
    private Map<String, Vertex> localXMLIdentifiers;

    /**
     * Map that stores names/identifiers for each Hierarchygraph while loading.
     */
    public static BiMap<Path, String> graphIds = new BiMap<>();

    private static Map<Path, HierarchyGraph> cache;

    /**
     * Instantiates a new Reader, and provide a set of vertex identifiers that may be used.
     * @param GlobalXMLIdentifiers A Map that provides mappings for vertices from strings used in the file format.
     */
    public Reader(Map<String, Vertex> GlobalXMLIdentifiers) {
        this.globalXMLIdentifiers = GlobalXMLIdentifiers;
        this.localXMLIdentifiers = new HashMap<>();
    }

    /**
     * Reads a hierarchy graph from a file.
     *
     * @param location the location
     * @return the hierarchy graph
     * @throws ParserConfigurationException the parser configuration exception
     * @throws IOException                  the io exception
     * @throws ParseException               the parse exception
     */
    public HierarchyGraph readFromString(Path location) throws IOException,  ParseException {
        return readFromString(Files.readString(location), location);
    }


    private static int fileCounter = 0;

    /**
     * Reads a hierarchygraph from a String.
     * @param contents The GraphML string that describes this hierarchygraph.
     * @param file     The file location of this hierarchygraph.
     * @return The hierarchygraph read from this string.
     * @throws IOException Thrown when a subgraph could not be read from a file.
     * @throws ParseException Thrown when the string contains syntax- or semantical errors.
     */
    public HierarchyGraph readFromString(String contents, Path file) throws IOException,  ParseException {
        if (cache.containsKey(file)) {
            return cache.get(file);
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        HierarchyGraph res = new HierarchyGraph();
        Document doc;
        try {
            doc = builder.parse(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException e) {
            throw new ParseException(file, e);
        }
        Element graphml = doc.getDocumentElement();
        if (!graphml.getTagName().equals("graphml")) {
            throw new ParseException(file, "Root tag must be \"graphml\".");
        }
        Node graph = graphml.getElementsByTagName("graph").item(0);
        if (graph == null) {
            throw new ParseException(file, "XML element \"graph\" not found.");
        }
        graphIds.put(file.toRealPath(), graph.getAttributes().getNamedItem("id").getTextContent() + fileCounter++);
        HierarchyGraph.namesOfHierarchyGraphs.put(res, graph.getAttributes().getNamedItem("id").getTextContent());
        for (int i = 0; i < graph.getChildNodes().getLength(); i++) {
            Node child = graph.getChildNodes().item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            switch (child.getNodeName()) {
                case "node":
                    addNode(res, child, file);
                    break;
                case "edge":
                    addEdge(res, child, file);
                    break;
                default:
                    throw new RuntimeException("Unsupported tag found: \"" + child.getNodeName() + "\"");
            }
        }
        cache.put(file, res);
        return res;
    }

    private void addEdge(HierarchyGraph graph, Node node, Path file) throws ParseException {
        String sourceString = node.getAttributes().getNamedItem("source").getTextContent();
        String targetString = node.getAttributes().getNamedItem("target").getTextContent();
        Vertex source;
        Vertex target;
        if (localXMLIdentifiers.containsKey(sourceString)) {
            source = localXMLIdentifiers.get(sourceString);
        } else {
            throw new ParseException(file, "Edge element references to unkown vertex identifier: \"" + sourceString + "\" in local context.");
        }
        if (localXMLIdentifiers.containsKey(targetString)) {
            target = localXMLIdentifiers.get(targetString);
        } else {
            throw new ParseException(file, "Edge element references to unkown vertex identifier: \"" + targetString + "\" in local context.");
        }
        graph.addEdge(source, target);
    }

    private Map<Path, HierarchyGraph> hierarchyGraphCache = new HashMap<>();
    private void addNode(HierarchyGraph graph, Node child, Path file) throws IOException, ParseException {
        Map<String, Set<String>> datamap = getDataMap(child);
        Set<Label> labels = new HashSet<>();
        if (datamap.containsKey("label")) {
            for (String stringLabel : datamap.get("label")) {
                labels.add(Labels.read(file, stringLabel));
            }
        } else {
            labels = Collections.emptySet();
        }
        String id = child.getAttributes().getNamedItem("id").getTextContent();
        if (globalXMLIdentifiers.containsKey(id)) {
            throw new RuntimeException("Duplicate node ID " + id);
        }
        if (datamap.containsKey("graphref")) {
            assert datamap.get("graphref").size() == 1;
            HierarchyGraph subgraph;
            Path pathToGraph = file.resolve("..").resolve(datamap.get("graphref").iterator().next()).toRealPath();
            if (!hierarchyGraphCache.containsKey(pathToGraph)) {
                subgraph = new Reader(globalXMLIdentifiers).readFromString(pathToGraph);
                hierarchyGraphCache.put(pathToGraph, subgraph);
            } else {
                subgraph = hierarchyGraphCache.get(pathToGraph);
            }
            Vertex toPut = graph.addComponent(subgraph, pathToGraph.toString());
            globalXMLIdentifiers.put(id, toPut);
            localXMLIdentifiers.put(id, toPut);

        } else if (datamap.containsKey("noderef")) {
            assert datamap.get("noderef").size() == 1;
            String to = datamap.get("noderef").iterator().next();
            if (!globalXMLIdentifiers.containsKey(to)) {
                throw new ParseException(file, "Port link made to unknown identifier: \"" + to + "\".");
            }
            Vertex toPut = graph.addPort(globalXMLIdentifiers.get(to), null);
            globalXMLIdentifiers.put(id, toPut);
            localXMLIdentifiers.put(id, toPut);
        } else {
            Vertex toPut = graph.addVertex(labels);
            globalXMLIdentifiers.put(id, toPut);
            localXMLIdentifiers.put(id, toPut);
        }
    }

    private Map<String, Set<String>> getDataMap(Node node) {
        NodeList children = node.getChildNodes();
        Map<String, Set<String>> res = new HashMap<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("data")) {
                String key = child.getAttributes().getNamedItem("key").getTextContent();
                String value = child.getTextContent();
                res.computeIfAbsent(key, x -> new HashSet<>());
                res.get(key).add(value);
            }
        }
        return res;
    }
}
