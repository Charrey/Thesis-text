package data.graph;

import java.util.Objects;

public class Edge {

    private final Node a;
    private final Node b;

    public Edge(Node a, Node b) {
        this.a = a;
        this.b = b;
    }

    public String toString() {
        return a + "--" + b;
    }

    public Edge reverse() {
        return new Edge(b, a);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return a.equals(edge.a) &&
                b.equals(edge.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }
}
