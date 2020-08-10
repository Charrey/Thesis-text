package charrey.writer;

import charrey.graph.HierarchyGraph;
import charrey.graph.Label;
import charrey.graph.Vertex;
import charrey.util.Labels;
import charrey.util.Util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Class used to write Hierarchygraphs to files.
 */
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

    private static Map<HierarchyGraph, String> seen = new HashMap<>();
    private static Map<HierarchyGraph, String> globalNameMap = new HashMap<>();

    /**
     * Exports a Hierarchygraph to a set of DOT strings.
     * @param graph The Hierarchygraph to export.
     * @param isMain Whether this hierarchygraph is the hierarchical top.
     * @param writeToDirectory If not null, refers to where the resultin DOT-files should be saved.
     * @return An object containing Strings in DOT-format that can be read into the original graph.
     */
    public static Export export(HierarchyGraph graph, boolean isMain, Path writeToDirectory) throws IOException {
        globalNameMap.putAll(HierarchyGraph.namesOfHierarchyGraphs);
        Map<String, String> res = new HashMap<>();
        StringBuilder sb = new StringBuilder(getPrefix(globalNameMap.getOrDefault(graph, "exportedGraph")));
        for (Map.Entry<Vertex, HierarchyGraph> entry : graph.getHierarchy().entrySet()) {
            if (seen.containsKey(entry.getValue())) {
                sb.append("\t\t<node id=\"n" + entry.getKey().getID() + "\">" + getLabels(entry.getKey()) + "<charrey.data key=\"graphref\">" + seen.get(entry.getValue()) + "</charrey.data></node>\n");
            } else {
                Export subExport = export(entry.getValue(), false, null);
                res.putAll(subExport.subFiles);
                seen.put(entry.getValue(), subExport.mainFile);
                sb.append("\t\t<node id=\"n" + entry.getKey().getID() + "\">" + getLabels(entry.getKey()) + "<charrey.data key=\"graphref\">" + subExport.mainFile + "</charrey.data></node>\n");
            }

        }
        for (Map.Entry<Vertex, Vertex> entry : graph.getPortMapping().entrySet()) {
            sb.append("\t\t<node id=\"n" + entry.getKey().getID() + "\">" + getLabels(entry.getKey()) + "<charrey.data key=\"noderef\">n" + entry.getValue().getID() + "</charrey.data></node>\n");
        }
        for (Vertex vertex : graph.getVertices()) {
            if (graph.getPortMapping().containsKey(vertex) || graph.getHierarchy().containsKey(vertex)) {
                continue;
            }
            sb.append("\t\t<node id=\"n" + vertex.getID() + "\">" + getLabels(vertex) + "</node>\n");
        }
        for (Map.Entry<Vertex, Set<Vertex>> edges : graph.getEdges().entrySet()) {
            for (Vertex target : edges.getValue()) {
                if (target.getID() < edges.getKey().getID()) {
                    continue;
                }
                sb.append("\t\t<edge id=\"" + edges.getKey().getID() +"-" + target.getID() + "\" source=\"n" + edges.getKey().getID() + "\" target=\"n" + target.getID() + "\"/>\n");
            }
        }

        String myFileName = isMain ? "main.graphml" : getNextFileName();
        res.put(myFileName, sb.append(SUFFIX).toString());
        Export toReturn = new Export(myFileName, res);
        if (writeToDirectory != null) {
            writeToDirectory(toReturn, writeToDirectory);
        }
        return toReturn;
    }

    private static String getLabels(Vertex key) {
        StringBuilder sb = new StringBuilder();
        for (Label label : key.getLabels()) {
            sb.append("<charrey.data key=\"label\">" + Labels.write(label) + "</charrey.data>");
        }
        return sb.toString();
    }

    private static void writeToDirectory(Export export, Path path) throws IOException {
        filenameCounter = 0;
        Util.makeDirectories(path);
        for (Map.Entry<String, String> entry : export.subFiles.entrySet()) {
            java.io.Writer writer = null;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path.resolve(entry.getKey()).toAbsolutePath().toString()), "utf-8"));
                writer.write(entry.getValue());
            }  finally {
                writer.close();
                }
            }
        }

        public static void writeToFile(String toWrite, Path path) throws IOException {
            Util.makeDirectories(path.getParent());
            java.io.Writer writer = null;
            try {
                if (!Files.exists(path)) {Files.createFile(path);}
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path.toRealPath().toString()), StandardCharsets.UTF_8));
                writer.write(toWrite);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                writer.close();
            }
        }


    private static class Export {

        /**
         * The Main file.
         */
        public final String mainFile;
        /**
         * The Sub files.
         */
        public final Map<String, String> subFiles;

        /**
         * Instantiates a new Export.
         *
         * @param mainFile the main file
         * @param subFiles the sub files
         */
        public Export(String mainFile, Map<String, String> subFiles) {
            this.mainFile = mainFile;
            this.subFiles = Collections.unmodifiableMap(subFiles);
        }
    }
}
