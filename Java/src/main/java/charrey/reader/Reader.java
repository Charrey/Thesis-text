package reader;

import data.graph.HierarchyGraph;
import data.graph.Label;
import data.graph.Vertex;
import exceptions.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import util.BiMap;
import util.Labels;

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
 * The type Reader.
 */
public class Reader {

    /**
     * Instantiates a new Reader.
     */
    public Reader() {
        this(new HashMap<>());
        cache = new HashMap<>();
    }

    private Map<String, Vertex> globalXMLIdentifiers;
    private Map<String, Vertex> localXMLIdentifiers;

    /**
     * The constant graphIds.
     */
    public static BiMap<Path, String> graphIds = new BiMap<>();

    private static Map<Path, HierarchyGraph> cache;

    /**
     * Instantiates a new Reader.
     *
     * @param GlobalXMLIdentifiers the global xml identifiers
     */
    public Reader(Map<String, Vertex> GlobalXMLIdentifiers) {
        this.globalXMLIdentifiers = GlobalXMLIdentifiers;
        this.localXMLIdentifiers = new HashMap<>();
    }

    /**
     * Read hierarchy graph.
     *
     * @param location the location
     * @return the hierarchy graph
     * @throws ParserConfigurationException the parser configuration exception
     * @throws IOException                  the io exception
     * @throws ParseException               the parse exception
     */
    public HierarchyGraph read(Path location) throws ParserConfigurationException, IOException,  ParseException {
        return read(new String(Files.readAllBytes(location), StandardCharsets.UTF_8), location);
    }


    private static int fileCounter = 0;

    /**
     * Read hierarchy graph.
     *
     * @param contents the contents
     * @param file     the file
     * @return the hierarchy graph
     * @throws ParserConfigurationException the parser configuration exception
     * @throws IOException                  the io exception
     * @throws ParseException               the parse exception
     */
    public HierarchyGraph read(String contents, Path file) throws ParserConfigurationException, IOException,  ParseException {
        if (cache.containsKey(file)) {
            return cache.get(file);
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
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
    private void addNode(HierarchyGraph graph, Node child, Path file) throws IOException, ParserConfigurationException, ParseException {
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
                subgraph = new Reader(globalXMLIdentifiers).read(pathToGraph);
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
