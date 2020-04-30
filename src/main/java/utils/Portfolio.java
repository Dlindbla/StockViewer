package utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Portfolio implements Serializable {

    public double liquidity;
    public ArrayList<LongPosition> longPositions;
    public String name;

    public Portfolio(String name, double initialLiquidity) {
        this.name = name;
        this.liquidity = initialLiquidity;
    }

    public void buyPosition(String ticker, Date buyDate, double buyPrice, int quantity){
        String uniqueID = UUID.randomUUID().toString();
        LongPosition newPosition = new LongPosition(ticker,buyDate,buyPrice,quantity,uniqueID);
        longPositions.add(newPosition);
        liquidity = liquidity - (buyPrice * quantity);
    }

    public void sellPosition(String uniqueID, double sellPrice){
        for(var item : longPositions){
            if(item.getUniqueID() == uniqueID){
                double profit = item.sell(sellPrice);
                liquidity = liquidity + profit;
                longPositions.remove(item);
                return;
            }
        }
    }



}
