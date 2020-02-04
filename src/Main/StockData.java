package Main;

import org.json.simple.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class StockData {
    String stockSymbol;
    String interval;
    String JSONkey;
    String secondJSONKey;
    boolean isIntraDay;
    int tickAmount;

    ArrayList<StockTick> stockTicks = new ArrayList<>();

    public ArrayList<StockTick> getStockTicks() {
        return stockTicks;
    }

    public String getInterval() {
        return interval;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }


    public StockData(JSONObject JSONData, String stockSymbol, String interval) throws org.json.simple.parser.ParseException, ParseException {
        this.stockSymbol = stockSymbol;
        this.interval = interval;
        this.JSONkey = "Time Series " + "(" + interval + ")";
        this.secondJSONKey = interval + " Time Series";
        isIntraDay = true;
        tickAmount = 0;
        //use try both keys to check whether the data is intraday or not
        //overwrite the JSONkey if the data is not intraday

        if (JSONData.containsKey(secondJSONKey)) {
            this.JSONkey = this.secondJSONKey;
            System.out.println(secondJSONKey);
            isIntraDay = false;
        }

        fillStockDataArray(JSONData);
        sortArray();

    }

    private void fillStockDataArray(JSONObject JSONData) throws org.json.simple.parser.ParseException, ParseException {
        JSONObject stockData = JSONData;

        JSONObject data = (JSONObject) stockData.get(JSONkey);
        if (data == null) throw new org.json.simple.parser.ParseException(1);
        System.out.println(JSONkey);
        for (Object tick : data.keySet()) {
            JSONObject tempJSON = (JSONObject) data.get(tick);
            String datetimeString = tick.toString();
            Date dateTime = parseDate(datetimeString);
            double open = Double.parseDouble(tempJSON.get("1. open").toString());
            double high = Double.parseDouble(tempJSON.get("2. high").toString());
            double low = Double.parseDouble(tempJSON.get("3. low").toString());
            double close = Double.parseDouble(tempJSON.get("4. close").toString());
            double volume;

            //ITEMS FOR ADJUSTED DATA
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

            StockTick tempTick = new StockTick(dateTime, open, high, low, close, volume, datetimeString, adjusted_close, dividend_amount, split_coefficient);
            stockTicks.add(tempTick);
            tickAmount++;
        }
    }

    private Date parseDate(String dateTimeString) throws ParseException {
        String[] formats = new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};
        Date resultDate = null;
        for (String format : formats) {
            if (dateTimeString != null && format.length() == dateTimeString.length()) {
                SimpleDateFormat sdFormat = new SimpleDateFormat(format);
                resultDate = sdFormat.parse(dateTimeString);
                break;

            }
        }
        return resultDate;
    }

    public void sortArray() {
        Collections.sort(stockTicks);
    }

}