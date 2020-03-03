package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BiMap<P, S> {

    private final Map<P, S> byKey;
    private final Map<S, Set<P>> byValue;

    public BiMap() {
        byKey = new HashMap<>();
        byValue = new HashMap<>();
    }

    public void removeValues(Predicate<S> condition) {
        Set<S> values = byValue.keySet().stream().filter(condition).collect(Collectors.toSet());
        values.forEach(byValue::remove);
        new HashSet<>(byKey.entrySet()).forEach(psEntry -> {
            if (values.contains(psEntry.getValue())) {
                byKey.remove(psEntry.getKey());
            }
        });
    }

    private BiMap(Map<P, S> byKey, Map<S, P> byValue) {
        this.byKey = new HashMap<>();
        this.byValue = new HashMap<>();
    }

    public void put(P key, S value) {
        if (byKey.containsKey(key)) {
            byValue.get(byKey.get(key)).remove(key);
        }
        byValue.putIfAbsent(value, new HashSet<>());
        byValue.get(value).add(key);
        byKey.put(key, value);
    }

    public boolean containsKey(P key) {
        return byKey.containsKey(key);
    }

    public S get(P key) {
        return byKey.get(key);
    }

    public boolean containsValue(S value) {
        return byValue.containsKey(value);
    }

    public S getOrDefault(P key, S defaultValue) {
        return byKey.getOrDefault(key, defaultValue);
    }

    public Set<P> getByValue(S value) {
        return byValue.get(value);
    }

    public Map<P, S> getToMap() {
        return byKey;
    }

    public void putAll(Map<P, S> map) {
        for (Map.Entry<P, S> i : map.entrySet()) {
            put(i.getKey(), i.getValue());
        }
    }
}
