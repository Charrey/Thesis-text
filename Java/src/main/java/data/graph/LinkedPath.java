package data.graph;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class LinkedPath extends Path {

    private LinkedList<Node> content;

    public LinkedPath(Node... nodes) {
        content = new LinkedList<>();
        content.addAll(Arrays.asList(nodes));
    }

    @Override
    public Node source() {
        return content.getFirst();
    }

    @Override
    public Node target() {
        return content.getLast();
    }


    @Override
    public Iterator<Node> iterator() {
        return content.iterator();
    }
}
