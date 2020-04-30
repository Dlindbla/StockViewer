package utils;

import java.util.Date;

public class LongPosition {
    private String ticker;
    private Date buyDate;
    private double buyPrice;
    private int quantity;
    private String uniqueID;
    boolean active = true;



    public LongPosition(String ticker, Date buyDate, double buyPrice, int quantity, String uniqueID){
        this.ticker = ticker;
        this.buyDate = buyDate;
        this.buyPrice = buyPrice;
        this.quantity = quantity;
        this.uniqueID = uniqueID;
    }

    public double sell(double sellPrice){
        //calculate profit
        double priceDelta = buyPrice - sellPrice / quantity;
        active = false;
        return priceDelta;
    }

    public String getTicker() {
        return ticker;
    }

    public Date getBuyDate() {
        return buyDate;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUniqueID() {
        return uniqueID;
    }
}
