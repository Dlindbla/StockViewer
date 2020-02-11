package stockapi;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class StockApi {
  public ArrayList<StockData> query(ArrayList<String> symbols, String interval, String timeSeries, String dataType) throws ApiException {
    var response = new ArrayList<StockData>();

    for (var symbol : symbols) {
      // Build cache key
      var key = symbol + interval + timeSeries;

      // Add data if cache already contains data
      if (stockCache.contains(key)) {
        // Set data type
        var obj = stockCache.get(key);
        obj.dataType = dataType;

        response.add(obj);
        continue;
      }

      // Otherwise fetch and add to cache
      try {
        var stockData = queryInternal(symbol, interval, timeSeries, dataType);
        response.add(stockData);
        stockCache.add(key, stockData);
      } catch (ApiException ex) {
        throw ex;
      } catch (Exception ex) {
      }
    }

    return response;
  }

  public ArrayList<SearchResult> search(String searchString) throws ApiException {
    // Check if search is cached
    if (searchCache.contains(searchString)) {
      return searchCache.get(searchString);
    }

    // Otherwise fetch and add to cache
    var result = new ArrayList<SearchResult>();
    try {
      result = searchInternal(searchString);
      searchCache.add(searchString, result);
    } catch (ApiException ex) {
      throw ex;
    } catch (Exception ex) {
    }

    return result;
  }

  protected abstract StockData queryInternal(String symbol, String interval, String timeSeries, String dataType)
      throws ApiException, IOException, ParseException, java.text.ParseException;

  protected abstract ArrayList<SearchResult> searchInternal(String searchString)
      throws ApiException, IOException, ParseException;

  protected JSONObject makeJSONWebRequest(String baseUrl, HashMap<String, String> params)
      throws IOException, ParseException {
    // Build url
    boolean firstParam = true;
    for (var param : params.entrySet()) {
      baseUrl += String.format("%s%s=%s", (firstParam ? "?" : "&"), param.getKey(), param.getValue());
      if (firstParam)
        firstParam = false;
    }
    var url = new URL(baseUrl);
    System.out.println(url);

    // Make and parse web request
    JSONParser parser = new JSONParser();
    var obj = (JSONObject) parser.parse(new InputStreamReader(url.openStream()));

    return obj;
  }

  private DataCache<StockData> stockCache = new DataCache<StockData>();
  private DataCache<ArrayList<SearchResult>> searchCache = new DataCache<ArrayList<SearchResult>>();
}