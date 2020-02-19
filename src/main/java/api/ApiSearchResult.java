package api;

public class ApiSearchResult implements Comparable<ApiSearchResult> {
    String symbol;
    String name;
    String region;
    Float matchScore;

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public ApiSearchResult(String symbol, String name, String region, Float matchScore) {
        this.symbol = symbol;
        this.name = name;
        this.region = region;
        this.matchScore = matchScore;
    }

    public int compareTo(ApiSearchResult result) {
        return result.matchScore.compareTo(matchScore);
    }

    public String toString() {
        return (name + " : " + region);
    }
}
