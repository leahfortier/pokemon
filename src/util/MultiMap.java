package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MultiMap<K, V> {
    private final Map<K, List<V>> map;
    
    public MultiMap() {
        this.map = new HashMap<>();
    }
    
    public void put(K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }
        
        map.get(key).add(value);
    }
    
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }
    
    public List<V> get(K key) {
        return map.get(key);
    }
    
    public Set<Entry<K, List<V>>> entrySet() {
        return map.entrySet();
    }
}
