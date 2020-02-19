package api;

public class ApiException extends Exception {
  public ApiException(String error) {
    super(error);
  }
}