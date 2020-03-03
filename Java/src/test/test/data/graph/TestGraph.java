package data.graph;

import data.MappingFunction;
import org.junit.Test;
import test.MyTestCase;
import util.Util;

import java.util.Set;

import static org.junit.Assert.*;

public class TestGraph extends MyTestCase {

    @Test
    public void testAddEdge() {
        HierarchyGraph res = new HierarchyGraph();
        Vertex a = res.addVertex();
        Vertex b = res.addVertex();
        assertEquals(0, res.getEdges().get(a).size());
        res.addEdge(a, b);
        assertEquals(1, res.getEdges().get(a).size());
        assertEquals(1, res.getEdges().get(b).size());
        res.addEdge(b, a);
        assertEquals(1, res.getEdges().get(a).size());
        assertEquals(Set.of(a), res.getEdges().get(b));
        assertEquals(Set.of(b), res.getEdges().get(a));
    }

    @Test
    public void testLock() {
        HierarchyGraph res = new HierarchyGraph();
        HierarchyGraph subgraph = new HierarchyGraph();
        Vertex subNode2 = subgraph.addVertex();
        Vertex a = res.addVertex();
        Vertex b = res.addVertex();
        Vertex c = res.addVertex();
        Vertex component = res.addComponent(subgraph, "subgraph");
        res.addEdge(a, b);
        res.lock();
        try {
            res.addVertex();
            fail();
        } catch (UnsupportedOperationException e) {}
        try {
            res.addEdge(a, c);
            fail();
        } catch (UnsupportedOperationException e) {}
        try {
            res.addComponent(new HierarchyGraph(), "Foo");
            fail();
        } catch (UnsupportedOperationException e) {}
        try {
            res.addPort(subNode2, component);
            fail();
        } catch (UnsupportedOperationException e) {}
    }

    @Test
    public void testFlatten() {
        HierarchyGraph bottom = new HierarchyGraph();
        Vertex bottomNode = bottom.addVertex(Label.CLOCK_FRAME);

        HierarchyGraph middle = new HierarchyGraph();
        Vertex bottomComponent = middle.addComponent(bottom, "Bottom");
        Vertex portToBelow = middle.addPort(bottomNode, bottomComponent);
        Vertex portToAbove = middle.addVertex(Label.LUT);
        middle.addEdge(portToAbove, portToBelow);

        HierarchyGraph top = new HierarchyGraph();
        Vertex middleComponent = top.addComponent(middle, "Middle");
        top.addPort(portToAbove, middleComponent);

        HierarchyGraph.CopyInfo flattened = top.flatten();
        assertEquals(Set.of(portToAbove, bottomNode), flattened.getMap().keySet());
        assertEquals(2, flattened.getGraph().getVertices().size());
        assertEquals(1, flattened.getGraph().getVerticesByLabel(Label.CLOCK_FRAME).size());
        assertEquals(1, flattened.getGraph().getVerticesByLabel(Label.LUT).size());
        assertEquals(2, flattened.getGraph().getEdges().size());
    }

    @Test
    public void testDeepCopy() {
        HierarchyGraph bottom = new HierarchyGraph();
        Vertex bottomNode = bottom.addVertex(Label.CLOCK_FRAME);

        HierarchyGraph middle = new HierarchyGraph();
        Vertex bottomComponent = middle.addComponent(bottom, "Bottom");
        Vertex portToBelow = middle.addPort(bottomNode, bottomComponent);
        Vertex portToAbove = middle.addVertex(Label.LUT);
        middle.addEdge(portToAbove, portToBelow);

        HierarchyGraph top = new HierarchyGraph();
        Vertex middleComponent = top.addComponent(middle, "Middle");
        top.addPort(portToAbove, middleComponent);

        HierarchyGraph.CopyInfo copy = top.deepCopy();
        MappingFunction map = new MappingFunction(top, copy.getGraph(), copy.getMap());
        assertTrue(Util.isCorrect(map.getPartialMapping()));
    }

    @Test
    public void testRemoveNode() {
        HierarchyGraph graph = new HierarchyGraph();
        Vertex node = graph.addVertex(Label.PIN);
        graph.removeVertex(node);
        assertFalse(graph.getEdges().containsKey(node));
        assertTrue(graph.getVerticesByLabel(Label.PIN).isEmpty());

        graph = new HierarchyGraph();
        HierarchyGraph subgraph = new HierarchyGraph();
        Vertex component = graph.addComponent(subgraph, "Subgraph");
        graph.removeVertex(component);
        assertFalse(graph.getEdges().containsKey(component));
        assertTrue(graph.getVerticesByLabel(Label.COMPONENT).isEmpty());
        assertFalse(graph.getHierarchy().containsKey(component));

        graph = new HierarchyGraph();
        subgraph = new HierarchyGraph();
        Vertex subNode = subgraph.addVertex(Label.PIN);
        component = graph.addComponent(subgraph, "Subgraph");
        Vertex port = graph.addPort(subNode, component);
        graph.removeVertex(port);
        assertFalse(graph.getEdges().containsKey(port));
        assertTrue(graph.getVerticesByLabel(Label.PORT).isEmpty());
        assertFalse(graph.getPortMapping().containsKey(port));
    }

    @Test
    public void testAddNode() {
        HierarchyGraph graph = new HierarchyGraph();
        assertEquals(0, graph.getVertices().size());
        graph.addVertex(Label.CLOCK_FRAME);
        assertEquals(1, graph.getVertices().size());
        assertEquals(Set.of(Label.CLOCK_FRAME), graph.getVertices().iterator().next().getLabels());
    }

    @Test
    public void testAddComponent() {
        HierarchyGraph graph = new HierarchyGraph();
        HierarchyGraph subgraph = new HierarchyGraph();
        Vertex component = graph.addComponent(subgraph, "FOO");
        assertEquals(subgraph, graph.getHierarchy().get(component));
    }

    @Test
    public void testShuffleIdentifiers() {
        HierarchyGraph graph = new HierarchyGraph();
        Vertex a = graph.addVertex(Label.PIN);//.clone();
        Vertex b = graph.addVertex(Label.LUT);//.clone();
        Vertex c = graph.addVertex(Label.MUX);//.clone();
        graph.addEdge(a, b);
        graph.shuffleIdentifiers(420);
        Vertex newA = graph.getVerticesByLabel(Label.PIN).iterator().next();
        Vertex newB = graph.getVerticesByLabel(Label.LUT).iterator().next();
        Vertex newC = graph.getVerticesByLabel(Label.MUX).iterator().next();
        assertEquals(newA, b);
        assertEquals(newB, c);
        assertEquals(newC, a);
        assertTrue(graph.getEdges().get(newA).contains(newB));
        assertTrue(graph.getEdges().get(newB).contains(newA));
        assertFalse(graph.getEdges().get(a).contains(b));
        assertFalse(graph.getEdges().get(b).contains(a));
    }

    @Test
    public void testGetNodesByLabel() {
        HierarchyGraph graph = new HierarchyGraph();
        Vertex pin1 = graph.addVertex(Label.PIN);
        Vertex pin2 = graph.addVertex(Label.PIN);
        Vertex mux = graph.addVertex(Label.MUX, Label.PIN);
        assertEquals(Set.of(pin1, pin2, mux), graph.getVerticesByLabel(Label.PIN));
        assertEquals(Set.of(mux), graph.getVerticesByLabel(Label.MUX));
    }


}
