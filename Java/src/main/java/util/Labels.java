package util;

import data.graph.Label;
import exceptions.ParseException;
import org.eclipse.collections.api.bimap.BiMap;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;
import org.eclipse.collections.impl.tuple.Tuples;

import java.nio.file.Path;

public class Labels {

    private static HashBiMap<String, Label> map = new HashBiMap<>();

    static {
        map.add(Tuples.pair("port", Label.PORT));
        map.add(Tuples.pair("pin", Label.PIN));
        map.add(Tuples.pair("component", Label.COMPONENT));
        map.add(Tuples.pair("in", Label.IN));
        map.add(Tuples.pair("select", Label.SELECT));
        map.add(Tuples.pair("mux", Label.MUX));
        map.add(Tuples.pair("out", Label.OUT));
        map.add(Tuples.pair("lut", Label.LUT));
        map.add(Tuples.pair("sync", Label.SYNC_RESET));
        map.add(Tuples.pair("async", Label.ASYNC_RESET));
        map.add(Tuples.pair("set", Label.SET));
        map.add(Tuples.pair("gate", Label.GATE));
        map.add(Tuples.pair("option", Label.OPTION));
        map.add(Tuples.pair("switch", Label.SWITCH));
        map.add(Tuples.pair("flow_from", Label.FLOW_FROM));
        map.add(Tuples.pair("flow_to", Label.FLOW_TO));
        map.add(Tuples.pair("clock", Label.CLOCK));
        map.add(Tuples.pair("always_on", Label.ALWAYS_ON));

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
            return map.flip().get(label).getAny();
        } else {
            throw new RuntimeException("Unaccounted enum value \"" + label + "\"");
        }
    }
}
