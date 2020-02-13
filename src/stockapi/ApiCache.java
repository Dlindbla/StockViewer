package stockapi;

import java.util.HashMap;

import utils.PlottableObject;

public class ApiCache {
  private HashMap<String, PlottableObject> cache = new HashMap<String, PlottableObject>();

  protected void addToCache(String key, PlottableObject object) {
    cache.put(key, object);
  }

  protected void removeFromCache(String key) {
    cache.remove(key);
  }

  protected PlottableObject getFromCache(String key) {
    return cache.get(key);
  }

  protected boolean existsInCache(String key) {
    return cache.containsKey(key);
  }

  protected void clearCache() {
    cache.clear();
  }
}