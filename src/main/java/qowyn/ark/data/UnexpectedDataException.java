package qowyn.ark.data;

public class UnexpectedDataException extends Exception {

  private static final long serialVersionUID = 1L;

  public UnexpectedDataException() {
    super();
  }

  public UnexpectedDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnexpectedDataException(String message) {
    super(message);
  }

  public UnexpectedDataException(Throwable cause) {
    super(cause);
  }

}
