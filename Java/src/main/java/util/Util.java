package util;

import data.PartialMapping;
import data.graph.HierarchyGraph;
import data.graph.Label;
import data.graph.Vertex;
import org.apache.batik.swing.JSVGCanvas;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * Provides several utility functions.
 */
public class Util {


    /**
     * Makes all parent directories of a file if they do not exist.
     *
     * @param dir The file
     */
    public static void makeDirectories(Path dir) {
        if (!dir.toAbsolutePath().getParent().toFile().exists()) {
            makeDirectories(dir.toAbsolutePath().getParent());
        }
        dir.toFile().mkdir();
    }


    /**
     * View a Hierarchygraph in a GUI (blocking method)
     *
     * @param graph the graph to be shown.
     * @throws IOException Thrown when the temporary file directory is not available for reading or writing.
     */
    public static synchronized void view(HierarchyGraph graph) throws IOException {
        System.out.println("Flattening...");
        HierarchyGraph.CopyInfo flattened = graph.flatten();
        System.out.println("Exporting DOT...");
        String dot = flattened.getGraph().toDOT(true);
        makeDirectories(Paths.get(".temp"));
        new File("./.temp").deleteOnExit();
        System.out.println("Writing to file...");
        writeToFile(dot, Paths.get(".temp/graph.dot"));
        new File("./.temp/graph.dot").deleteOnExit();
        System.out.println("Calling FDP...");
        long baseTime = System.currentTimeMillis();
        System.out.println("Estimated time: " + estimateTime(flattened.getGraph()));
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("sfdp -Tsvg \".temp/graph.dot\"");
        BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            sb.append(line).append("\n");
        }
        pr.destroy();
        writeFDP(flattened.getGraph().getVertices().size(), flattened.getGraph().getEdges().entrySet().stream().reduce(0, (u, nodeSetEntry) -> u + nodeSetEntry.getValue().size(), Integer::sum) / 2, System.currentTimeMillis()-baseTime);
        Path svgFile = Paths.get(".temp/graph" + new Random().nextInt() +".svg");
        svgFile.toFile().deleteOnExit();
        writeToFile(sb.toString(), svgFile);
        SVGFrame frame = new SVGFrame(svgFile);
        frame.setVisible(true);
        while (frame.isVisible()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private static void writeFDP(int nodes, int edges, long time) throws IOException {
        File file = new File("./measurements/fdp.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        try {
            writer.write(nodes + "\t" + edges + "\t" + time + "\n");
        } finally {
            writer.close();
        }
    }

    private static long estimateTime(HierarchyGraph res) throws IOException {
        makeDirectories(Paths.get("./measurements"));
        new File("./measurements/fdp.txt").createNewFile();
        List<String> lines = Files.readAllLines(Paths.get("./measurements/fdp.txt"));
        if (lines.isEmpty()) {
            return -1;
        }
        else {
            for (String line : lines) {
                String[] splitted = line.split("\t");
                System.out.println(splitted[0] + "\t" + splitted[1] + "\t" + splitted[2]);
            }
        }
        return -1;
    }


    private static void writeToFile(String dot, Path path) throws IOException {
        Files.write(path, dot.getBytes(Charset.defaultCharset()));
    }

    /**
     * Provides a list view of a collection of several lists without modifying them.
     *
     * @param inputs A collection of lists
     * @return A list view functioning as the concatenation of these lists.
     */
    public static ConcatList concat(List<Vertex>... inputs) {
        return new ConcatList(inputs);
    }

    /**
     * Returns whether a partial mapping upholds the Path Subgraph Isomorphism constraints.
     *
     * @param f The partial mapping to check
     * @return Whether it satisfies the constraints.
     */
    public static boolean isCorrect(PartialMapping f) {
        return false;//TODO
    }

    /**
     * Checks several properties of Hierarchygraphs that should hold. Throws an AssertionError if a property fails to hold.
     *
     * @param graph the graph to check
     */
    public static void checkConsistent(HierarchyGraph graph) {
        for (Map.Entry<Vertex, Set<Vertex>> a : graph.getEdges().entrySet()) {
            if (!graph.getVertices().contains(a.getKey())) {
                assert  false;
            }
            if (!graph.getVertices().containsAll(a.getValue())) {
                assert false;
            }
        }
        for (Vertex vertex : graph.getVertices()) {
            for (Label label : vertex.getLabels()) {
                if (!graph.getVerticesByLabel(label).contains(vertex)) {
                    assert false;
                }
            }
            if (vertex.getLabels().contains(Label.PORT) && !graph.getPortMapping().containsKey(vertex)) {
                //assert false;
            }
            if (vertex.getLabels().contains(Label.COMPONENT) && !graph.getHierarchy().containsKey(vertex)) {
                assert false;
            }
            if (vertex.getLabels().contains(Label.COMPONENT)) {
                if (graph.getEdges().get(vertex).stream().anyMatch(node1 -> !node1.getLabels().contains(Label.PORT))) {
                    assert false;
                }
            }
        }
        return;
    }

    /**
     * Assert a condition or else display a specific message in a thrown AssertionError.
     *
     * @param assertion What is required to be true.
     * @param message   The message to be shown when the assertion fails.
     */
    public static void assertOrElse(boolean assertion, String message) {
        if (!assertion) {
            throw new AssertionError(message);
        }
    }

    /**
     * Provides a List of only one element, or an empty list if the argument is null.
     *
     * @param vertex The vertex to add to an element or null.
     * @return A list containing the given vertex or an empty list if it was null.
     */
    public static List<Vertex> listOf(Vertex vertex) {
        if (vertex != null) {
            return List.of(vertex);
        } else {
            return Collections.emptyList();
        }
    }


    private static class SVGFrame extends JFrame {
        private static final Object lock = new Object();

        /**
         * Instantiates a new SVGFrame that can display an SVG file.
         *
         * @param path The SVG file.
         */
        public SVGFrame(Path path) {
            JSVGCanvas svgCanvas = new JSVGCanvas();
            svgCanvas.setURI(path.toUri().toString());
            this.getContentPane().add(svgCanvas);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e){
                    synchronized (lock) {
                        setVisible(false);
                        lock.notify();
                    }
                }
            });
            pack();
        }

    }

    /**
     * A List view of several concatenated Lists.
     */
    public static class ConcatList implements List<Vertex> {

        private final List<Vertex>[] content;

        private ConcatList(){content = null;}

        /**
         * Instantiates a new Concat list from the provided Lists.
         * @param content The lists to concatenate.
         */
        public ConcatList(List<Vertex>[] content) {
            this.content = content;
        }

        @Override
        public int size() {
            return Arrays.stream(content).reduce(0, (u, object) -> u + object.size(), Integer::sum);
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object o) {
            return Arrays.stream(content).anyMatch(x -> x.contains(o));
        }

        @Override
        public Iterator<Vertex> iterator() {
            return new Iterator<>() {
                int listIndex = 0;
                int item = 0;

                @Override
                public boolean hasNext() {
                    if (content[listIndex].size() < item + 1) {
                        return true;
                    }
                    for (int i = listIndex + 1; i < content.length; i++) {
                        if (!content[i].isEmpty()) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public Vertex next() {
                    item++;
                    if (item >= content[listIndex].size()) {
                        item = 0;
                        listIndex++;
                        while (content[listIndex].isEmpty()) {
                            listIndex++;
                        }
                    }
                    return content[listIndex].get(item);
                }
            };
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException();        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();        }

        @Override
        public boolean add(Vertex node) {
            throw new UnsupportedOperationException();        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException();        }

        @Override
        public boolean addAll(Collection<? extends Vertex> c) {
            throw new UnsupportedOperationException();        }

        @Override
        public boolean addAll(int index, Collection<? extends Vertex> c) {
            throw new UnsupportedOperationException();        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Vertex get(int index) {
            int removeFromIndex = 0;
            for (List<Vertex> list : content) {
                if (list.size() > index - removeFromIndex) {
                    return list.get(index - removeFromIndex);
                } else {
                    removeFromIndex += list.size();
                }
            }
            return null;
        }

        @Override
        public Vertex set(int index, Vertex element) {
            throw new UnsupportedOperationException();        }

        @Override
        public void add(int index, Vertex element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Vertex remove(int index) {
            throw new UnsupportedOperationException();        }

        @Override
        public int indexOf(Object o) {
            throw new UnsupportedOperationException();        }

        @Override
        public int lastIndexOf(Object o) {
            throw new UnsupportedOperationException();        }

        @Override
        public ListIterator<Vertex> listIterator() {
            throw new UnsupportedOperationException();        }

        @Override
        public ListIterator<Vertex> listIterator(int index) {
            throw new UnsupportedOperationException();        }

        @Override
        public List<Vertex> subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();        }

    }
}
