package stockapi;

import java.util.List;
import java.util.Map;

public class ApiInfo {
  public String name;
  public List<String> intervals;
  public Map<String, List<String>> dataTypes;

  public ApiInfo(String name, List<String> intervals, Map<String, List<String>> dataTypes) {
    this.name = name;
    this.intervals = intervals;
    this.dataTypes = dataTypes;
  }
}