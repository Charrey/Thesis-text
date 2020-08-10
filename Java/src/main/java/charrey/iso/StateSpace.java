package charrey.iso;

import charrey.graph.HierarchyGraph;

import java.util.ArrayDeque;
import java.util.Deque;

public class StateSpace {

    private Deque<State> stack;

    public StateSpace(HierarchyGraph virtual, HierarchyGraph concrete) {
        stack = new ArrayDeque<>();
        stack.push(State.unconstrained(virtual, concrete));
    }
}
