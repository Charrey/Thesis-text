package util;

import data.graph.Label;
import exceptions.ParseException;

import java.nio.file.Path;

public class Labels {

    private static BiMap<String, Label> map = new BiMap<>();

    static {
        map.put("port", Label.PORT);
        map.put("pin", Label.PIN);
        map.put("component", Label.COMPONENT);
        map.put("in", Label.IN);
        map.put("select", Label.SELECT);
        map.put("mux", Label.MUX);
        map.put("out", Label.OUT);
        map.put("lut", Label.LUT);
        map.put("sync_reset", Label.SYNC_RESET);
        map.put("async_reset", Label.ASYNC_RESET);
        map.put("sync_set", Label.SYNC_SET);
        map.put("async_set", Label.ASYNC_SET);
        map.put("clock_enable", Label.CLOCK_ENABLE);
        map.put("rising_or_falling_edge", Label.CLOCK_FRAME);
        map.put("gate", Label.GATE);
        map.put("option", Label.OPTION);
        map.put("switch", Label.SWITCH);
        map.put("flow_from", Label.FLOW_FROM);
        map.put("flow_to", Label.FLOW_TO);
        map.put("clock", Label.CLOCK);
        map.put("always_on", Label.EXTRA);
        map.put("ERROR", Label.REMOVE);
        map.put("register", Label.REGISTER);

    }

    public static Label read(Path file, String textContent) throws ParseException {
       if (map.containsKey(textContent)) {
           return map.get(textContent);
       } else {
           throw new ParseException(file, "Unknown label \"" + textContent + "\"");
       }
    }

    public static String write(Label label) {
        if (map.containsValue(label)) {
            return map.getByValue(label).iterator().next();
        } else {
            throw new RuntimeException("Unaccounted enum value \"" + label + "\"");
        }
    }
}
