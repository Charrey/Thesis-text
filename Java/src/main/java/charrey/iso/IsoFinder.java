package charrey.iso;

import charrey.data.PartialMapping;
import charrey.graph.HierarchyGraph;
import charrey.exceptions.NoMappingException;

/**
 * Class that finds path subgraph isomorphisms between two hierarchygraphs.
 */
public class IsoFinder {

    /**
     * Returns a path subgraph isomorphism between two hierarchygraphs.0
     * @param virtual Hierarchygraph to be mapped onto another hierarchygraph.
     * @param concrete Hierarchygraph on which the other has to be mapped.
     * @return A mapping that follows the path subgraph isomorphism constraint.
     * @throws NoMappingException Thrown when no such mapping could be found.
     */
    public static PartialMapping getMapping(HierarchyGraph virtual, HierarchyGraph concrete) throws NoMappingException {
        throw new NoMappingException();
    }
}
