package utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class IniSettings {
  private final static Properties properties = new Properties();

  public static void read(String fileName) throws IOException {
    properties.load(new FileInputStream(fileName));
  }

  public static void write(String fileName) throws IOException {
    properties.store(new FileOutputStream(fileName), "StockViewer");
  }

  public static String get(String key) {
    return properties.getProperty(key);
  }

  public static String[] getArray(String key) {
    var val = get(key);

    // Split by comma
    var items = val.split(",");
    for (int i = 0; i < items.length; i++) {
      items[i] = items[i].replaceAll("\\s+", "");
    }

    return items;
  }

  public static void set(String key, String value) {
    properties.setProperty(key, value);
  }
}