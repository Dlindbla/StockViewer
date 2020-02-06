package stockapi;

import java.util.HashMap;

public class DataCache<T> {
  private HashMap<String, T> cache = new HashMap<String, T>();

  public void add(String key, T data) {
    cache.put(key, data);
  }

  public void remove(String key) {
    cache.remove(key);
  }

  public T get(String key) {
    return cache.get(key);
  }

  public boolean contains(String key) {
    return cache.containsKey(key);
  }

  public void clear() {
    cache.clear();
  }
}