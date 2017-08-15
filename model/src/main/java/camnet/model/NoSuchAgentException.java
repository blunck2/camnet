package camnet.model;

public class NoSuchAgentException extends Exception {
  public NoSuchAgentException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoSuchAgentException(String message) {
    super(message);
  }

}