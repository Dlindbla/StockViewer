package Main;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.util.ArrayList;

public class StockDataGenerator{

    public static ArrayList<StockData> getArrayList (ArrayList<String> symbolStrings, String interval, String timeSeries) throws IOException, ParseException, java.text.ParseException {
        ArrayList<StockData> stockDataArrayList = new ArrayList<StockData>();
        for (String symbol : symbolStrings) {
            String url = URLBuilder.queryString(timeSeries, symbol, interval, true);
            System.out.println(url);
            //GET JSON object
            JSONObject jsonData = GetJSONData.getJsonFromUrl(url);
            //Parse to StockData object
            StockData stockDataObject = new StockData(jsonData, symbol, interval);
            //Feed into the Generator to create a XYChart.Series object
            stockDataArrayList.add(stockDataObject);
        }
        return stockDataArrayList;
    }

}
