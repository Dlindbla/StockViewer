package stockapi;

import java.util.ArrayList;

import utils.PlottableObject;

public interface Api {
  public ApiInfo getInfo();
  public ArrayList<String> search(String keyword);
  public PlottableObject query(String symbol, String interval, String dataType);

  public void addToCache(String key, PlottableObject object);
  public void removeFromCache(String key);
  public PlottableObject getFromCache(String key);
  public boolean existsInCache(String key);
  public void clearCache();
}