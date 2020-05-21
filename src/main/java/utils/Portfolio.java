package utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;
    public double liquidity;
    public ArrayList<LongPosition> longPositions;
    public String name;

    public Portfolio(String name, double initialLiquidity) {
        this.name = name;
        this.liquidity = initialLiquidity;
        this.longPositions = new ArrayList<LongPosition>();
    }

    public void buyPosition(String ticker, Date buyDate, double buyPrice, int quantity){
        String uniqueID = UUID.randomUUID().toString();
        LongPosition newPosition = new LongPosition(ticker, buyDate, buyPrice, quantity, uniqueID);
        longPositions.add(newPosition);
        liquidity -= buyPrice * quantity;
    }

    public void sellPosition(LongPosition position, double sellPrice) {
        double profit = position.sell(sellPrice);
        liquidity += profit;
        longPositions.remove(position);
    }

    @Override
    public String toString(){
        return name;
    }
}




