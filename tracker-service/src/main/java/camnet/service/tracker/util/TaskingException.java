package camnet.service.tracker.util;

public class TaskingException extends Exception {
  public TaskingException(String message) {
    super(message);
  }

  public TaskingException(String message, Throwable cause) {
    super(message, cause);
  }
}