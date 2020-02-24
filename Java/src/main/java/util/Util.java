package util;

import data.graph.HierarchyGraph;
import data.graph.Node;
import org.eclipse.collections.api.bimap.BiMap;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
}
