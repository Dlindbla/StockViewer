package stockapi;

import java.util.ArrayList;

public class StockData {
    String symbol;
    String interval;
    String dataType;
    ArrayList<StockTick> ticks = new ArrayList<StockTick>();

    public String getSymbol() {
        return symbol;
    }

    public String getInterval() {
        return interval;
    }

    public String getDataType() {
        return dataType;
    }

    public int getTickAmount() {
        return ticks.size();
    }

    public ArrayList<StockTick> getTicks() {
        return ticks;
    }

    public void addTick(StockTick tick) {
        ticks.add(tick);
    }

    public StockData(String symbol, String interval, String dataType) {
        this.symbol = symbol;
        this.interval = interval;
        this.dataType = dataType;
    }
}
