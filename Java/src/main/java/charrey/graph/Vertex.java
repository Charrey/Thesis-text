package charrey.data.graph;

import charrey.util.Labels;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A single vertex in a hierarchical graph.
 */
public class Vertex implements Cloneable {

    private Set<Label> labels;
    private int ID;
    private static int IDCounter = 0;
    private boolean locked = false;
    private HierarchyGraph graph;

    /**
     * Instantiates a new Vertex with a set of labels.
     * @param labels The labels
     */
    public Vertex(Label... labels) {
        this.labels = Set.of(labels);
        ID = IDCounter++;
    }

    /**
     * Instantiates a new Vertex with a set of labels.
     *
     * @param labels The labels
     */
    public Vertex(Set<Label> labels) {
        this.labels = new HashSet<>(labels);
        ID = IDCounter++;
    }

    public String toString() {
        return String.valueOf(ID) + labels;
    }

    /**
     * Returns an identifier for this vertex. These are arbitrary and should only be used when in comparisons where the outcome
     * is irrelevant but consistent.
     * @return The identifier
     */
    public int getID() {
        return ID;
    }

    /**
     * Returns a string declaring this vertex in DOT format
     * @return the string
     */
    public String getDOT() {
        return ID + "[label=\"" + labels.stream().map(Labels::write).collect(Collectors.toSet()) + "\"]";
    }

    public Vertex clone() {
        Vertex next = new Vertex();
        next.locked = locked;
        next.labels = new HashSet<>(labels);
        next.ID = ID;
        return next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return ID == vertex.ID &&
                labels.equals(vertex.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }

    /**
     * Returns the labels of this vertex.
     *
     * @return the labels of this vertex.
     */
    public Set<Label> getLabels() {
        return labels;
    }

    /**
     * Sets labels if this vertex. Caution: if this vertex is indexed, that index should be changed as well!
     *
     * @param labels The new set of labels.
     */
    public void setLabels(Set<Label> labels) {
        if (locked) {
            throw new UnsupportedOperationException("Vertex is locked!");
        } else {
            this.labels = labels;
        }
    }

    /**
     * Permanently restricts any changes to this Vertex (i.e. make it immutable).
     */
    public void lock() {
        locked = true;
    }

}
