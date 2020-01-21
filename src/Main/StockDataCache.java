package Main;

import java.util.ArrayList;
import java.util.Collection;

public class StockDataCache {

    private ArrayList<StockData> stockDataArray = new ArrayList<>();
    private ArrayList<String> stringList = new ArrayList<>();

    public boolean contains(String itemString){
        return stringList.contains(itemString);
    }

    public int indexOf(String itemString){
        return stringList.indexOf(itemString);
    }

    public void addAll(Collection<StockData> data){
        for (StockData item : data){
            add(item);
        }
    }

    public void add(StockData item){
        stockDataArray.add(item);
        String dataString = item.getStockSymbol().concat(item.getInterval());
        stringList.add(dataString);
    }

    public void remove(int index){
        stockDataArray.remove(index);
        stringList.remove(index);
    }

    public void clear(){
        stockDataArray.clear();
        stringList.clear();
    }

    public StockData get(int index){
        return stockDataArray.get(index);
    }
}
