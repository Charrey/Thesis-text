package data.graph;

/**
 * A Path from one Vertex to another.
 */
public abstract class Path implements Iterable<Vertex> {

    /**
     * First Vertex on this path
     * @return The first vertex.
     */
    public abstract Vertex source();

    /**
     * Last Vertex on this path.
     *
     * @return The last vertex.
     */
    public abstract Vertex target();


}
