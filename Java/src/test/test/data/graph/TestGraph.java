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
        Node a = res.addNode();
        Node b = res.addNode();
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
        Node subNode2 = subgraph.addNode();
        Node a = res.addNode();
        Node b = res.addNode();
        Node c = res.addNode();
        Node component = res.addComponent(subgraph, "subgraph");
        res.addEdge(a, b);
        res.lock();
        try {
            res.addNode();
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
        Node bottomNode = bottom.addNode(Label.CLOCK);

        HierarchyGraph middle = new HierarchyGraph();
        Node bottomComponent = middle.addComponent(bottom, "Bottom");
        Node portToBelow = middle.addPort(bottomNode, bottomComponent);
        Node portToAbove = middle.addNode(Label.LUT);
        middle.addEdge(portToAbove, portToBelow);

        HierarchyGraph top = new HierarchyGraph();
        Node middleComponent = top.addComponent(middle, "Middle");
        top.addPort(portToAbove, middleComponent);

        HierarchyGraph.CopyInfo flattened = top.flatten();
        assertEquals(Set.of(portToAbove, bottomNode), flattened.getMap().keySet());
        assertEquals(2, flattened.getGraph().getNodes().size());
        assertEquals(1, flattened.getGraph().getNodesByLabel(Label.CLOCK).size());
        assertEquals(1, flattened.getGraph().getNodesByLabel(Label.LUT).size());
        assertEquals(2, flattened.getGraph().getEdges().size());
    }

    @Test
    public void testDeepCopy() {
        HierarchyGraph bottom = new HierarchyGraph();
        Node bottomNode = bottom.addNode(Label.CLOCK);

        HierarchyGraph middle = new HierarchyGraph();
        Node bottomComponent = middle.addComponent(bottom, "Bottom");
        Node portToBelow = middle.addPort(bottomNode, bottomComponent);
        Node portToAbove = middle.addNode(Label.LUT);
        middle.addEdge(portToAbove, portToBelow);

        HierarchyGraph top = new HierarchyGraph();
        Node middleComponent = top.addComponent(middle, "Middle");
        top.addPort(portToAbove, middleComponent);

        HierarchyGraph.CopyInfo copy = top.deepCopy();
        MappingFunction map = new MappingFunction(top, copy.getGraph(), copy.getMap());
        assertTrue(Util.isCorrect(map.getPartialMapping()));
    }

    @Test
    public void testRemoveNode() {
        HierarchyGraph graph = new HierarchyGraph();
        Node node = graph.addNode(Label.PIN);
        graph.removeNode(node);
        assertFalse(graph.getEdges().containsKey(node));
        assertTrue(graph.getNodesByLabel(Label.PIN).isEmpty());

        graph = new HierarchyGraph();
        HierarchyGraph subgraph = new HierarchyGraph();
        Node component = graph.addComponent(subgraph, "Subgraph");
        graph.removeNode(component);
        assertFalse(graph.getEdges().containsKey(component));
        assertTrue(graph.getNodesByLabel(Label.COMPONENT).isEmpty());
        assertFalse(graph.getHierarchy().containsKey(component));

        graph = new HierarchyGraph();
        subgraph = new HierarchyGraph();
        Node subNode = subgraph.addNode(Label.PIN);
        component = graph.addComponent(subgraph, "Subgraph");
        Node port = graph.addPort(subNode, component);
        graph.removeNode(port);
        assertFalse(graph.getEdges().containsKey(port));
        assertTrue(graph.getNodesByLabel(Label.PORT).isEmpty());
        assertFalse(graph.getPortMapping().containsKey(port));
    }

    @Test
    public void testAddNode() {
        HierarchyGraph graph = new HierarchyGraph();
        assertEquals(0, graph.getNodes().size());
        graph.addNode(Label.CLOCK);
        assertEquals(1, graph.getNodes().size());
        assertEquals(Set.of(Label.CLOCK), graph.getNodes().iterator().next().getLabels());
    }

    @Test
    public void testAddComponent() {
        HierarchyGraph graph = new HierarchyGraph();
        HierarchyGraph subgraph = new HierarchyGraph();
        Node component = graph.addComponent(subgraph, "FOO");
        assertEquals(subgraph, graph.getHierarchy().get(component));
    }

    @Test
    public void testShuffleIdentifiers() {
        HierarchyGraph graph = new HierarchyGraph();
        Node a = graph.addNode(Label.PIN);//.clone();
        Node b = graph.addNode(Label.LUT);//.clone();
        Node c = graph.addNode(Label.MUX);//.clone();
        graph.addEdge(a, b);
        graph.shuffleIdentifiers(420);
        Node newA = graph.getNodesByLabel(Label.PIN).iterator().next();
        Node newB = graph.getNodesByLabel(Label.LUT).iterator().next();
        Node newC = graph.getNodesByLabel(Label.MUX).iterator().next();
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
        Node pin1 = graph.addNode(Label.PIN);
        Node pin2 = graph.addNode(Label.PIN);
        Node mux = graph.addNode(Label.MUX, Label.PIN);
        assertEquals(Set.of(pin1, pin2, mux), graph.getNodesByLabel(Label.PIN));
        assertEquals(Set.of(mux), graph.getNodesByLabel(Label.MUX));
    }


}
