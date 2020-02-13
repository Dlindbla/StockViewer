package utils;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.Date;


public class PlottableObject {

    private String name;
    private String currency;

    private ArrayList<Pair<Date, Number>> items;

    public PlottableObject(String name, String currency, ArrayList<Pair<Date, Number>> items ){
        this.name = name;
        this.currency = currency;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public ArrayList<Pair<Date, Number>> getItems() {
        return items;
    }


}
