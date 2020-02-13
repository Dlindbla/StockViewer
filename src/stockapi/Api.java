package stockapi;

import java.util.ArrayList;

import utils.PlottableObject;

public interface Api {
  public ApiInfo getInfo();
  public ArrayList<ApiSearchResult> search(String keyword) throws ApiException;
  public PlottableObject query(String symbol, String interval, String dataType) throws ApiException;
}