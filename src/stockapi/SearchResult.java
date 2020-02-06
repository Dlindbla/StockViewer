package stockapi;

public class SearchResult implements Comparable<SearchResult> {
    String symbol;
    String name;
    String type;
    String region;
    String marketOpen;
    String marketClose;
    String timezone;
    String currency;
    Float matchScore;

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getRegion() {
        return region;
    }

    public String getMarketOpen() {
        return marketOpen;
    }

    public String getMarketClose() {
        return marketClose;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getCurrency() {
        return currency;
    }

    public Float getMatchScore() {
        return matchScore;
    }

    public SearchResult(String symbol, String name, String type, String region, String marketOpen, String marketClose,
            String timezone, String currency, Float matchScore) {
        this.symbol = symbol;
        this.name = name;
        this.type = type;
        this.region = region;
        this.marketOpen = marketOpen;
        this.marketClose = marketClose;
        this.timezone = timezone;
        this.currency = currency;
        this.matchScore = matchScore;
    }

    @Override
    public int compareTo(SearchResult searchResultObject) {
        return searchResultObject.matchScore.compareTo(this.matchScore);
    }

    @Override
    public String toString() {
        return (this.getName() + " : " + getRegion());
    }
}
