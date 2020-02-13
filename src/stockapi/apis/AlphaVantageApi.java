package stockapi.apis;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.util.Pair;
import stockapi.Api;
import stockapi.ApiCache;
import stockapi.ApiException;
import stockapi.ApiInfo;
import stockapi.ApiSearchResult;
import utils.PlottableObject;

public class AlphaVantageApi extends ApiCache implements Api {
  private final List<String> intervals = Arrays.asList("15min", "5min", "1min", "Monthly", "Weekly", "Daily");
  private final List<String> intradayDataTypes = Arrays.asList("Open", "High", "Low", "Close", "Volume");
  private final List<String> adjustedDataTypes = Arrays.asList("Open", "High", "Low", "Close", "Volume",
      "Adjusted close", "Dividend amount");

  private final Map<String, List<String>> dataTypes = Map.of(
    "15min", intradayDataTypes,
    "5min", intradayDataTypes,
    "1min", intradayDataTypes,
    "Monthly", adjustedDataTypes,
    "Weekly", adjustedDataTypes,
    "Daily", adjustedDataTypes
  );
  private final Map<String, String> timeSeries = Map.of(
    "15min", "TIME_SERIES_INTRADAY",
    "5min", "TIME_SERIES_INTRADAY",
    "1min", "TIME_SERIES_INTRADAY",
    "Monthly", "TIME_SERIES_MONTHLY_ADJUSTED",
    "Weekly", "TIME_SERIES_WEEKLY_ADJUSTED",
    "Daily", "TIME_SERIES_DAILY_ADJUSTED"
  );
  private final Map<String, String> dataTypeKeys = Map.of(
    "Open", "1. open",
    "High", "2. high",
    "Low", "3. low",
    "Close", "4. close",
    "Volume", ". volume", // volume can be either 5 or 6
    "Adjusted close", "5. adjusted close"
  );

  private final String[] dateFormats = new String[] { "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd" };

  public ApiInfo getInfo() {
    return new ApiInfo("Alpha Vantage", intervals, dataTypes);
  }

  public ArrayList<ApiSearchResult> search(String keyword) throws ApiException {
    // Build params
    var params = new HashMap<String, String>();
    params.put("function", "SYMBOL_SEARCH");
    params.put("keywords", keyword);

    // Make web request and parse data
    JSONObject json;
    try {
      json = makeQueryRequest(params);
    } catch (Exception ex) {
      throw new ApiException(ex.getMessage());
    }

    var searchResults = new ArrayList<ApiSearchResult>();

    // Parse json
    var results = (JSONArray) json.get("bestMatches");
    if (!results.isEmpty()) {
      for (var resultValue : results.toArray()) {
        var result = (JSONObject) resultValue;

        var symbol = result.get("1. symbol").toString();
        var name = result.get("2. name").toString();
        var region = result.get("4. region").toString();
        var matchScore = Float.parseFloat(result.get("9. matchScore").toString());

        searchResults.add(new ApiSearchResult(symbol, name, region, matchScore));
      }
    }

    return searchResults;
  }

  public PlottableObject query(String symbol, String interval, String dataType) throws ApiException {
    // Build params
    var params = new HashMap<String, String>();
    params.put("function", timeSeries.get(interval));
    params.put("symbol", symbol);
    params.put("interval", interval);
    params.put("outputsize", "full");

    // Check cache
    var cacheKey = symbol + interval + dataType;
    if (existsInCache(cacheKey)) {
      return getFromCache(cacheKey);
    }

    // Make web request
    JSONObject json;
    try {
      json = makeQueryRequest(params);
    } catch (Exception ex) {
      throw new ApiException(ex.getMessage());
    }
    
    // Find data by removing "Meta Data" key and using the remaining one
    json.keySet().remove("Meta Data");
    var stockData = (JSONObject) json.get(json.keySet().toArray()[0]);

    var items = new HashMap<String, ArrayList<Pair<Date, Number>>>();

    // Loop through all keys
    for (var tickKey : stockData.keySet()) {
      var tick = (JSONObject) stockData.get(tickKey);

      // Parse date
      Date date = null;
      for (var format : dateFormats) {
        if (format.length() == tickKey.toString().length()) {
          try {
            var sdFormat = new SimpleDateFormat(format);
            date = sdFormat.parse(tickKey.toString());
          } catch (java.text.ParseException ex) {
            throw new ApiException(ex.getMessage());
          }

          break;
        }
      }

      // Set all dataTypes
      for (var type : dataTypeKeys.entrySet()) {
        // Get data value
        var value = findJsonKeyValue(type.getValue(), tick);
        if (value == null) continue;

        // Get or create new list
        var arr = items.getOrDefault(type.getKey(), new ArrayList<Pair<Date, Number>>());

        // Add current <date data> pair to list
        arr.add(new Pair<Date, Number>(date, Double.parseDouble(value.toString())));
        items.put(type.getKey(), arr);
      }
    }

    // Populate cache
    for (var item : items.entrySet()) {
      addToCache(symbol + interval + item.getKey(), new PlottableObject(symbol, "$", item.getValue()));
    }

    // Return item from cache
    return getFromCache(cacheKey);
  }

  private Object findJsonKeyValue(String searchKey, JSONObject object) {
    // Test exact match
    var match = object.get(searchKey);
    if (match == null) {
      // Attempt to find key that contains our value
      for (var key : object.keySet()) {
        if (key.toString().contains(searchKey)) {
          match = object.get(key);
          break;
        }
      }
    }

    return match;
  }

  private JSONObject makeQueryRequest(HashMap<String, String> params) throws IOException, ParseException, ApiException {
    // Set api key
    params.put("apikey", Double.toString(Math.random()));

    // Build url
    var baseUrl = "https://www.alphavantage.co/query";
    boolean firstParam = true;
    for (var param : params.entrySet()) {
      baseUrl += String.format("%s%s=%s", (firstParam ? "?" : "&"), param.getKey(), param.getValue());
      if (firstParam)
        firstParam = false;
    }
    var url = new URL(baseUrl);
    System.out.println(url); // debug print

    // Make and parse web request
    var res = (JSONObject) new JSONParser().parse(new InputStreamReader(url.openStream()));

    // Check if rate limited
    if (res.containsKey("Note")) {
      throw new ApiException("Rate limit possibly hit: " + res.get("Note").toString());
    }

    return res;
  }
}