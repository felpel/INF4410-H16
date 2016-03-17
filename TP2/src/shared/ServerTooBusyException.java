package shared;

@SuppressWarnings("serial")
public class ServerTooBusyException extends Exception {
  public ServerTooBusyException() { super(); }
  public ServerTooBusyException(String message) { super(message); }
  public ServerTooBusyException(Throwable cause) { super(cause); }
  public ServerTooBusyException(String message, Throwable cause) { super(message, cause); }
}
