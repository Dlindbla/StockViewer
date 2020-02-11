package stockapi;

public class ApiException extends Exception {
  public ApiException(String error) {
    super(error);
  }
}