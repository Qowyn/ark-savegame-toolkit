package qowyn.ark;

import java.util.Formatter;
import java.util.function.Supplier;

public class LoggerHelper {

  public static Supplier<String> format(String msg, Object... objects) {
    return () -> {
      try (Formatter formatter = new Formatter()) {
        formatter.format(msg, objects);
        return formatter.toString();
      }
    };
  }

}
