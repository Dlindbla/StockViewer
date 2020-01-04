package Main;

public class URLBuilder {

    public static String queryString(String timeSeries,String symbol,String interval,boolean setFull){
        StringBuilder builder = new StringBuilder();
        builder.append("https://www.alphavantage.co/query?function=");
        builder.append(timeSeries);
        builder.append("&symbol=");
        builder.append(symbol);
        if(!interval.isEmpty()) {
            builder.append("&interval=");
            builder.append(interval);
        }
        if(setFull) {
            builder.append("&outputsize=full");
        }
        builder.append("&apikey=");
        builder.append(Math.random());
        String result = builder.toString();
        return result;
    }


    //    https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=BA&apikey=demo
    public static String searchString(String keyword){
        StringBuilder builder = new StringBuilder();
        builder.append("https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=");
        builder.append(keyword);
        builder.append("&apikey=");
        builder.append(Math.random());
        String searchString = builder.toString();
        return searchString;
    }


}
