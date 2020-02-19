package main;


import data.graph.HierarchyGraph;
import data.graph.Label;
import data.graph.Node;
import data.graph.Patterns;

public class Main {

    public static void main(String[] args) {
        HierarchyGraph hg = new HierarchyGraph();
        Node pin1 = hg.addNode(Label.PIN);
        Node pin2 = hg.addNode(Label.PIN);
        Node pin3 = hg.addNode(Label.PIN);
        Node pin4 = hg.addNode(Label.PIN);

        Patterns.MUX mux = Patterns.MUX(1);

        Node component = hg.addComponent(mux.hierarchyGraph);
        Node port1 = hg.addPort(mux.in1.get(0));
        Node port2 = hg.addPort(mux.in2.get(0));
        Node port3 = hg.addPort(mux.out.get(0));
        Node port4 = hg.addPort(mux.select);
        hg.addEdge(pin1, port1);
        hg.addEdge(pin2, port2);
        hg.addEdge(pin3, port3);
        hg.addEdge(pin4, port4);
        hg.addEdge(port1, component);
        hg.addEdge(port2, component);
        hg.addEdge(port3, component);
        hg.addEdge(port4, component);

        //System.out.println(hg.toDOT());
        System.out.println(hg.flatten().toDOT());
    }

}
