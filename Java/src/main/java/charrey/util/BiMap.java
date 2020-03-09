package charrey.util;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A Bidirectional Map.
 *
 * @param <P> The key type
 * @param <S> The value type
 */
public class BiMap<P, S> {

    private final Map<P, S> byKey;
    private final Map<S, Set<P>> byValue;

    /**
     * Instantiates a new BiMap.
     */
    public BiMap() {
        byKey = new HashMap<>();
        byValue = new HashMap<>();
    }

    public BiMap(Map<P, S> init) {
        byKey = new HashMap<>();
        byValue = new HashMap<>();
        putAll(init);
    }

    /**
     * Remove any entry from this BiMap where the value matches the condition.
     * @param condition which satisfied for values, results in the entry being removed.
     */
    public void removeValues(Predicate<S> condition) {
        Set<S> values = byValue.keySet().stream().filter(condition).collect(Collectors.toSet());
        values.forEach(byValue::remove);
        new HashSet<>(byKey.entrySet()).forEach(psEntry -> {
            if (values.contains(psEntry.getValue())) {
                byKey.remove(psEntry.getKey());
            }
        });
    }

    /**
     * Puts a new value in this BiMap
     * @param key   The key
     * @param value The value
     */
    public void put(P key, S value) {
        if (byKey.containsKey(key)) {
            byValue.get(byKey.get(key)).remove(key);
        }
        byValue.putIfAbsent(value, new HashSet<>());
        byValue.get(value).add(key);
        byKey.put(key, value);
    }

    /**
     * Returns whether the given object is a key in this BiMap.
     * @param key The object to check whether it's a key.
     * @return Whether the object is a key in this map.
     */
    public boolean containsKey(P key) {
        return byKey.containsKey(key);
    }

    /**
     * Gets a value given its associated key.
     * @param key The key
     * @return The value associated with that key.
     */
    public S get(P key) {
        return byKey.get(key);
    }

    /**
     * Returns whether the given object is a value in this BiMap.
     * @param value The object to check whether it's a value.
     * @return Whether the object is a value in this map.
     */
    public boolean containsValue(S value) {
        return byValue.containsKey(value);
    }

    /**
     * Gets by value.
     *
     * @param value the value
     * @return the by value
     */
    public Set<P> getByValue(S value) {
        return byValue.get(value);
    }

    /**
     * Returns a view of this map in one direction.
     * @return The view of this map.
     */
    public Map<P, S> getToMap() {
        return Collections.unmodifiableMap(byKey);
    }

    /**
     * Puts all values in this BiMap from a Map.
     * @param map The Map from which to get the values.
     */
    public void putAll(Map<P, S> map) {
        for (Map.Entry<P, S> i : map.entrySet()) {
            put(i.getKey(), i.getValue());
        }
    }
}
