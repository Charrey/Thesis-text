package data.graph;

import util.Labels;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Node implements Cloneable {

    private Set<Label> labels;
    private int ID;
    private static int IDCounter = 0;
    private boolean locked = false;
    private HierarchyGraph graph;

    public Node(Label... labels) {
        this.labels = Set.of(labels);
        ID = IDCounter++;
    }

    public Node(Set<Label> labels) {
        this.labels = new HashSet<>(labels);
        ID = IDCounter++;
    }

    public String toString() {
        return String.valueOf(ID) + labels;
    }

    public int getID() {
        return ID;
    }

    public String getDOT() {
        return ID + "[label=\"" + labels.stream().map(Labels::write).collect(Collectors.toSet()) + "\"]";
    }

    public Node clone() {
        Node next = new Node();
        next.locked = locked;
        next.labels = new HashSet<>(labels);
        next.ID = ID;
        return next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return ID == node.ID &&
                labels.equals(node.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public void setLabels(Set<Label> labels) {
        if (locked) {
            throw new UnsupportedOperationException("Node is locked!");
        } else {
            this.labels = labels;
        }
    }

    public void lock() {
        locked = true;
    }

}
