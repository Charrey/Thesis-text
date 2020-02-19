package util;

import data.graph.Label;

public class Labels {

    public static Label get(String textContent) {
        switch (textContent) {
            case "port":
                return Label.PORT;
            case "pin":
                return Label.PIN;
            case "component":
                return Label.COMPONENT;
            case "in":
                return Label.IN;
            case "select":
                return Label.SELECT;
            case "mux":
                return Label.MUX;
            case "out":
                return Label.OUT;
            default:
                throw new RuntimeException("Unknown label " + textContent);
        }
    }
}
