package util;

import data.PartialMapping;
import data.graph.HierarchyGraph;
import data.graph.Node;
import org.apache.batik.swing.JSVGCanvas;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Util {
    public static void makeDirectories(Path dir) {
        if (!dir.toAbsolutePath().getParent().toFile().exists()) {
            makeDirectories(dir.toAbsolutePath().getParent());
        }
        dir.toFile().mkdir();
    }


    public static void assertConnected(HierarchyGraph res) {
        Set<Node> seen = new HashSet<>();
        seen.add(res.getNodes().iterator().next());
        int previoussize = -1;
        while (seen.size() != previoussize) {
            previoussize = seen.size();
            for (Node node : new HashSet<>(seen)) {
                seen.addAll(res.getEdges().get(node));
            }
        }
        assert seen.size() == res.getNodes().size();
    }

    public static synchronized void view(HierarchyGraph res) throws IOException {
        System.out.println("Flattening...");
        HierarchyGraph flattened = res.flatten();
        System.out.println("Exporting DOT...");
        String dot = flattened.toDOT(true);
        makeDirectories(Paths.get(".temp"));
        System.out.println("Writing to file...");
        writeToFile(dot, Paths.get(".temp/graph.dot"));
        System.out.println("Calling FDP...");
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("fdp -Tsvg \".temp/graph.dot\"");
        BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            sb.append(line).append("\n");
            //System.out.println("SDP output: " + line);
        }
        Path svgFile = Paths.get(".temp/graph" + new Random().nextInt() +".svg");
        writeToFile(sb.toString(), svgFile);
        Files.delete(Paths.get("./.temp/graph.dot"));
        //Files.delete(Paths.get("./.temp"));
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


    private static void writeToFile(String dot, Path path) throws IOException {
        Files.write(path, dot.getBytes(Charset.defaultCharset()));
    }

    public static boolean isRecursiveCall() {
        StackTraceElement method = Thread.currentThread().getStackTrace()[2];
        String className = method.getClassName();
        String methodName = method.getMethodName();
        for (int i = 3; i < Thread.currentThread().getStackTrace().length; i++) {
            method = Thread.currentThread().getStackTrace()[i];
            if (className.equals(method.getClassName()) && methodName.equals(method.getMethodName())) {
                return true;
            }
        }
        return false;
    }

    public static List<Node> concat(List<Node>... inputs) {
        return new ConcatList(inputs);
    }

    public static boolean isCorrect(PartialMapping f) {
        return false;
    }


    private static class SVGFrame extends JFrame {
        public static boolean isVisible;
        private static final Object lock = new Object();
        protected JSVGCanvas svgCanvas = new JSVGCanvas();

        public SVGFrame(Path path) {
            isVisible = true;
            svgCanvas.setURI(path.toUri().toString());
            this.getContentPane().add(svgCanvas);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e){
                    //your code to be executed before window is closed.
                    //not the place to opt out closing window
                    synchronized (lock) {
                        try {
                            isVisible = false;
                            Files.delete(path);
                            setVisible(false);
                        } catch (IOException f) {
                            System.err.println("Could not delete files...");
                        }
                        lock.notify();
                    }
                }
            });
            pack();
        }
    }

    private static class ConcatList implements List<Node> {

        private final List<Node>[] content;

        public ConcatList(List<Node>[] content) {
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
        public Iterator<Node> iterator() {
            return  new Iterator<Node>() {
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
                public Node next() {
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
        public boolean add(Node node) {
            throw new UnsupportedOperationException();        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException();        }

        @Override
        public boolean addAll(Collection<? extends Node> c) {
            throw new UnsupportedOperationException();        }

        @Override
        public boolean addAll(int index, Collection<? extends Node> c) {
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
        public Node get(int index) {
            int removeFromIndex = 0;
            for (List<Node> list : content) {
                if (list.size() > index - removeFromIndex) {
                    return list.get(index - removeFromIndex);
                } else {
                    removeFromIndex += list.size();
                }
            }
            return null;
        }

        @Override
        public Node set(int index, Node element) {
            throw new UnsupportedOperationException();        }

        @Override
        public void add(int index, Node element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Node remove(int index) {
            throw new UnsupportedOperationException();        }

        @Override
        public int indexOf(Object o) {
            throw new UnsupportedOperationException();        }

        @Override
        public int lastIndexOf(Object o) {
            throw new UnsupportedOperationException();        }

        @Override
        public ListIterator<Node> listIterator() {
            throw new UnsupportedOperationException();        }

        @Override
        public ListIterator<Node> listIterator(int index) {
            throw new UnsupportedOperationException();        }

        @Override
        public List<Node> subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();        }

    }
}
