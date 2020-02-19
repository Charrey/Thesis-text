package main;
import data.graph.HierarchyGraph;
import data.graph.Label;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import util.Labels;

import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Reader {

    public Reader() {
        this(new HashMap<>());
        cache = new HashMap<>();
    }

    private Map<String, data.graph.Node> XMLIdentifiers;
    private static Map<Path, HierarchyGraph> cache;

    public Reader(Map<String, data.graph.Node> XMLIdentifiers) {
        this.XMLIdentifiers = XMLIdentifiers;
    }

    public HierarchyGraph read(Path file) throws ParserConfigurationException, IOException, SAXException {
        if (cache.containsKey(file)) {
            return cache.get(file);
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        HierarchyGraph res = new HierarchyGraph();
        ByteArrayInputStream input = new ByteArrayInputStream(Files.readAllBytes(file));
        Document doc = builder.parse(input);
        Element graphml = doc.getDocumentElement();
        Node graph = graphml.getElementsByTagName("graph").item(0);
        for (int i = 0; i < graph.getChildNodes().getLength(); i++) {
            Node child = graph.getChildNodes().item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            switch (child.getNodeName()) {
                case "node":
                    addNode(res, child, file.getParent());
                    break;
                case "edge":
                    addEdge(res, child);
                    break;
                default:
                    throw new RuntimeException("Unsupported tag found: \"" + child.getNodeName() + "\"");
            }
        }
        cache.put(file, res);
        return res;
    }

    private void addEdge(HierarchyGraph graph, Node node) {
        data.graph.Node source = XMLIdentifiers.get(node.getAttributes().getNamedItem("source").getTextContent());
        data.graph.Node target = XMLIdentifiers.get(node.getAttributes().getNamedItem("target").getTextContent());
        graph.addEdge(source, target);
    }

    private void addNode(HierarchyGraph graph, Node child, Path directory) throws IOException, SAXException, ParserConfigurationException {
        Map<String, Set<String>> datamap = getDataMap(child);
        Set<Label> labels = datamap.get("label").stream().map(Labels::get).collect(Collectors.toSet());
        String id = child.getAttributes().getNamedItem("id").getTextContent();
        if (XMLIdentifiers.containsKey(id)) {
            throw new RuntimeException("Duplicate node ID " + id);
        }
        if (datamap.containsKey("graphref")) {
            assert datamap.get("graphref").size() == 1;
            Path pathToGraph = directory.resolve(datamap.get("graphref").iterator().next());
            HierarchyGraph subgraph = new Reader(XMLIdentifiers).read(pathToGraph);
            XMLIdentifiers.put(id, graph.addComponent(subgraph));
        } else if (datamap.containsKey("noderef")) {
            assert datamap.get("noderef").size() == 1;
            String to = datamap.get("noderef").iterator().next();
            XMLIdentifiers.put(id, graph.addPort(XMLIdentifiers.get(to)));
        } else {
            XMLIdentifiers.put(id, graph.addNode(labels));
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
