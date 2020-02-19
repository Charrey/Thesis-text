package data.graph;

import java.util.Objects;
import java.util.Set;

public class Node {

    private Set<Label> labels;
    private int ID;
    private static int IDCounter = 0;

    public Node(Label... labels) {
        this.labels = Set.of(labels);
        ID = IDCounter++;
    }

    public Node(Set<Label> labels) {
        this.labels = labels;
        ID = IDCounter++;
    }

    public String toString() {
        return String.valueOf(ID) + labels;
    }

    public int getID() {
        return ID;
    }

    public String getDOT() {
        return ID + "[label=\"" + labels + "\"]";
    }

    public void setID(int i) {
        ID = i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return ID == node.ID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }
}
