package qowyn.ark.properties;

public class UnreadablePropertyException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public UnreadablePropertyException() {
    super();
  }

  public UnreadablePropertyException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnreadablePropertyException(String message) {
    super(message);
  }

  public UnreadablePropertyException(Throwable cause) {
    super(cause);
  }

}
