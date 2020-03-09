package charrey.data.graph;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A LinkedList implementation of Path.
 */
public class LinkedPath extends Path {

    private LinkedList<Vertex> content;

    /**
     * Instantiates a new LinkedPath.
     * @param vertices The vertices that make up the path.
     */
    public LinkedPath(Vertex... vertices) {
        content = new LinkedList<>();
        content.addAll(Arrays.asList(vertices));
    }

    @Override
    public Vertex source() {
        return content.getFirst();
    }

    @Override
    public Vertex target() {
        return content.getLast();
    }


    @Override
    public Iterator<Vertex> iterator() {
        return content.iterator();
    }
}
