package stockapi.apis;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import stockapi.SearchResult;
import stockapi.StockApi;
import stockapi.StockData;
import stockapi.StockTick;

public class AlphaVantage extends StockApi {
  private final String baseApiUrl = "https://www.alphavantage.co/query";

  protected StockData queryInternal(String symbol, String interval, String timeSeries)
      throws IOException, ParseException, java.text.ParseException {
    // Build params
    var params = new HashMap<String, String>();
    params.put("function", timeSeries);
    params.put("symbol", symbol);
    if (!interval.isEmpty()) {
      params.put("interval", interval);
    }
    params.put("outputsize", "full");
    params.put("apikey", Double.toString(Math.random()));

    // Make web request and parse data
    var json = makeJSONWebRequest(baseApiUrl, params);
    var data = parseStockData(json, symbol, interval);

    return data;
  }

  protected ArrayList<SearchResult> searchInternal(String searchString) throws IOException, ParseException {
    // Build params
    var params = new HashMap<String, String>();
    params.put("function", "SYMBOL_SEARCH");
    params.put("keywords", searchString);
    params.put("apikey", Double.toString(Math.random()));

    // Make web request and parse data
    var json = makeJSONWebRequest(baseApiUrl, params);
    var result = parseSearchResult(json);

    return new ArrayList<SearchResult>(result);
  }

  private StockData parseStockData(JSONObject object, String stockSymbol, String interval)
      throws java.text.ParseException {
    var stock = new StockData(stockSymbol, interval);
    JSONObject stockData;
    if (object.containsKey(interval + " Time Series")) {
      stockData = (JSONObject) object.get(interval + " Time Series");
    } else {
      stockData = (JSONObject) object.get("Time Series " + "(" + interval + ")");
    }

    for (var tick : stockData.keySet()) {
      var tempJSON = (JSONObject) stockData.get(tick);
      var datetimeString = tick.toString();
      var dateTime = parseDate(datetimeString);
      var open = Double.parseDouble(tempJSON.get("1. open").toString());
      var high = Double.parseDouble(tempJSON.get("2. high").toString());
      var low = Double.parseDouble(tempJSON.get("3. low").toString());
      var close = Double.parseDouble(tempJSON.get("4. close").toString());
      double volume;

      // ITEMS FOR ADJUSTED DATA
      double adjusted_close = 0;
      double dividend_amount = 0;
      double split_coefficient = 0;
      if (tempJSON.containsKey("5. adjusted close")) {
        adjusted_close = Double.parseDouble(tempJSON.get("5. adjusted close").toString());
        dividend_amount = Double.parseDouble(tempJSON.get("7. dividend amount").toString());
        split_coefficient = Double.parseDouble(tempJSON.get("8. split coefficient").toString());
        volume = Double.parseDouble(tempJSON.get("6. volume").toString());
      } else {
        volume = Double.parseDouble(tempJSON.get("5. volume").toString());
      }

      var tempTick = new StockTick(dateTime, open, high, low, close, volume, datetimeString, adjusted_close,
          dividend_amount, split_coefficient);

      stock.addTick(tempTick);
    }

    return stock;
  }

  private ArrayList<SearchResult> parseSearchResult(JSONObject object) {
    var res = new ArrayList<SearchResult>();

    JSONArray results = (JSONArray) object.get("bestMatches");
    if (!(results.isEmpty())) {
      for (var resultValue : results.toArray()) {
        JSONObject resultValueAsJSON = (JSONObject) resultValue;
        String symbol = resultValueAsJSON.get("1. symbol").toString();
        String name = resultValueAsJSON.get("2. name").toString();
        String type = resultValueAsJSON.get("3. type").toString();
        String region = resultValueAsJSON.get("4. region").toString();
        String marketOpen = resultValueAsJSON.get("5. marketOpen").toString();
        String marketClose = resultValueAsJSON.get("6. marketClose").toString();
        String timezone = resultValueAsJSON.get("7. timezone").toString();
        String currency = resultValueAsJSON.get("8. currency").toString();
        Float matchScore = Float.parseFloat((String) resultValueAsJSON.get("9. matchScore"));
        var result = new SearchResult(symbol, name, type, region, marketOpen, marketClose, timezone, currency,
            matchScore);
        res.add(result);
      }
    } else {
      System.out.println("The results-list is empty!");
    }

    return res;
  }

  private Date parseDate(String dateTimeString) throws java.text.ParseException {
    String[] formats = new String[] { "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd" };
    Date resultDate = null;
    for (var format : formats) {
      if (dateTimeString != null && format.length() == dateTimeString.length()) {
        SimpleDateFormat sdFormat = new SimpleDateFormat(format);
        resultDate = sdFormat.parse(dateTimeString);
        break;
      }
    }
    return resultDate;
  }
}