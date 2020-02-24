package reader;
import data.graph.HierarchyGraph;
import data.graph.Label;
import exceptions.ParseException;
import org.eclipse.collections.api.bimap.BiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;
import org.eclipse.collections.impl.tuple.Tuples;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import util.Labels;
import util.Util;

import javax.xml.parsers.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Reader {

    public Reader() {
        this(new HashMap<>());
        cache = new HashMap<>();
    }

    private Map<String, data.graph.Node> globalXMLIdentifiers;
    private Map<String, data.graph.Node> localXMLIdentifiers;
    public static HashBiMap<Path, String> graphIds = new HashBiMap<>();

    private static Map<Path, HierarchyGraph> cache;

    public Reader(Map<String, data.graph.Node> GlobalXMLIdentifiers) {
        this.globalXMLIdentifiers = GlobalXMLIdentifiers;
        this.localXMLIdentifiers = new HashMap<>();
    }

    public HierarchyGraph read(Path location) throws ParserConfigurationException, IOException,  ParseException {
        return read(new String(Files.readAllBytes(location), StandardCharsets.UTF_8), location);
    }


    private static int fileCounter = 0;
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
        res.getNamesOfHierarchyGraphs().put(res, graph.getAttributes().getNamedItem("id").getTextContent());
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
        data.graph.Node source;
        data.graph.Node target;
        if (localXMLIdentifiers.containsKey(sourceString)) {
            source = localXMLIdentifiers.get(sourceString);
        } else {
            throw new ParseException(file, "Edge element references to unkown node identifier: \"" + sourceString + "\" in local context.");
        }
        if (localXMLIdentifiers.containsKey(targetString)) {
            target = localXMLIdentifiers.get(targetString);
        } else {
            throw new ParseException(file, "Edge element references to unkown node identifier: \"" + targetString + "\" in local context.");
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
            data.graph.Node toPut = graph.addComponent(subgraph, pathToGraph.toString());
            globalXMLIdentifiers.put(id, toPut);
            localXMLIdentifiers.put(id, toPut);

        } else if (datamap.containsKey("noderef")) {
            assert datamap.get("noderef").size() == 1;
            String to = datamap.get("noderef").iterator().next();
            if (!globalXMLIdentifiers.containsKey(to)) {
                throw new ParseException(file, "Port link made to unknown identifier: \"" + to + "\".");
            }
            data.graph.Node toPut = graph.addPort(globalXMLIdentifiers.get(to), null);
            globalXMLIdentifiers.put(id, toPut);
            localXMLIdentifiers.put(id, toPut);
        } else {
            data.graph.Node toPut = graph.addNode(labels);
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
