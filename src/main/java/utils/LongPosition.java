package utils;

import java.io.Serializable;
import java.util.Date;

public class LongPosition implements Serializable {
    private static final long serialVersionUID = 1L;
    private String ticker;
    private Date buyDate;
    private double buyPrice;
    private double currentPrice;
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

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getPriceDelta() {
        return currentPrice - buyPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalValue() {
        return currentPrice * quantity;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setCurrentPrice(double price) {
        currentPrice = price;
    }
}
