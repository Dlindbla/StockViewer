package stockapi;

import java.util.ArrayList;

public class StockData {
    String symbol;
    String interval;
    ArrayList<StockTick> ticks = new ArrayList<>();

    public String getSymbol() {
        return symbol;
    }

    public String getInterval() {
        return interval;
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

    public StockData(String symbol, String interval) {
        this.symbol = symbol;
        this.interval = interval;
    }
}