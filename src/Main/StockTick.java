package Main;

import java.util.Date;

public class StockTick implements Comparable<StockTick> {
    Date dateTime;
    double open;
    double high;
    double low;
    double close;
    double volume;
    String rawDate;
    long longDate;
    double adjusted_close;
    double dividend_amount;
    double split_coefficient;

    public StockTick(Date dateTime, double open, double high, double low, double close, double volume, String rawDate, double adjusted_close, double dividend_amount,double split_coefficient) {
        this.dateTime = dateTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.rawDate = rawDate;
        this.longDate = dateTime.getTime();

        //Values for ADJUSTED TIME SPLITS
        this.adjusted_close = adjusted_close;
        this.dividend_amount = dividend_amount;
        this.split_coefficient = split_coefficient;
    }

    @Override
    public int compareTo(StockTick o) {
        return dateTime.compareTo(o.dateTime);
    }
}

