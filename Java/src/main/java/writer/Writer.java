package writer;

import data.graph.HierarchyGraph;
import data.graph.Label;
import data.graph.Node;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import reader.Reader;
import util.Labels;
import util.Util;


public class Writer {

    private static String getPrefix(String name) {
        return "<?xml version=\"1.0\" encoding=\"UTF8\"?>\n" +
                "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n" +
                "\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema−instance\"\n" +
                "\txsi:schemaLocation=\n" +
                "\t\"http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n" +
                "\t<key id=\"label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>\n" +
                "\t<key id=\"graphref\" for=\"node\" attr.name=\"is hierarchygraph\" attr.type=\"string\"/>\n" +
                "\t<key id=\"noderef\" for=\"node\" attr.name=\"linked to\" attr.type=\"string\"/>\n" +
                "\t<graph id=\"" + name + "\" edgedefault=\"undirected\">\n\"";
    }

    private static final String SUFFIX = "\t</graph>\n</graphml>";
    private static int filenameCounter = 0;
    private static String getNextFileName() {
        return "graph_" + filenameCounter++ + ".graphml";
    }


    /**
     *
     * @param graph
     * @param isMain
     * @return Map from filename to filecontent
     */
    private static Map<HierarchyGraph, String> seen = new HashMap<>();
    private static Map<HierarchyGraph, String> globalNameMap = new HashMap<>();
    public static Pair<String, Map<String, String>> export(HierarchyGraph graph, boolean isMain) {
        globalNameMap.putAll(graph.getNamesOfHierarchyGraphs());
        Map<String, String> res = new HashMap<>();
        StringBuilder sb = new StringBuilder(getPrefix(globalNameMap.getOrDefault(graph, "exportedGraph")));
        for (Map.Entry<Node, HierarchyGraph> entry : graph.getHierarchy().entrySet()) {
            if (seen.containsKey(entry.getValue())) {
                sb.append("\t\t<node id=\"n" + entry.getKey().getID() + "\">" + getLabels(entry.getKey()) + "<data key=\"graphref\">" + seen.get(entry.getValue()) + "</data></node>\n");
            } else {
                Pair<String, Map<String, String>> subExport = export(entry.getValue(), false);
                res.putAll(subExport.getTwo());
                seen.put(entry.getValue(), subExport.getOne());
                sb.append("\t\t<node id=\"n" + entry.getKey().getID() + "\">" + getLabels(entry.getKey()) + "<data key=\"graphref\">" + subExport.getOne()+ "</data></node>\n");
            }

        }
        for (Map.Entry<Node, Node> entry : graph.getPortMapping().entrySet()) {
            sb.append("\t\t<node id=\"n" + entry.getKey().getID() + "\">" + getLabels(entry.getKey()) + "<data key=\"noderef\">n" + entry.getValue().getID() + "</data></node>\n");
        }
        for (Node node : graph.getNodes()) {
            if (graph.getPortMapping().containsKey(node) || graph.getHierarchy().containsKey(node)) {
                continue;
            }
            sb.append("\t\t<node id=\"n" + node.getID() + "\">" + getLabels(node) + "</node>\n");
        }
        for (Map.Entry<Node, Set<Node>> edges : graph.getEdges().entrySet()) {
            for (Node target : edges.getValue()) {
                if (target.getID() < edges.getKey().getID()) {
                    continue;
                }
                sb.append("\t\t<edge id=\"" + edges.getKey().getID() +"-" + target.getID() + "\" source=\"n" + edges.getKey().getID() + "\" target=\"n" + target.getID() + "\"/>\n");
            }
        }

        String myFileName = isMain ? "main.graphml" : getNextFileName();
        res.put(myFileName, sb.append(SUFFIX).toString());
        return Tuples.pair(myFileName, res);
    }

    private static String getLabels(Node key) {
        StringBuilder sb = new StringBuilder();
        for (Label label : key.getLabels()) {
            sb.append("<data key=\"label\">" + Labels.write(label) + "</data>");
        }
        return sb.toString();
    }

    public static void writeToDirectory(Pair<String, Map<String, String>> content, Path path) throws IOException {
        filenameCounter = 0;
        Util.makeDirectories(path);
        for (Map.Entry<String, String> entry : content.getTwo().entrySet()) {
            java.io.Writer writer = null;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path.resolve(entry.getKey()).toAbsolutePath().toString()), "utf-8"));
                writer.write(entry.getValue());
            }  finally {
                writer.close();
                }
            }
        }


}
