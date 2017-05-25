package qowyn.ark.types;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ArkName {

  private static final Pattern NAME_INDEX_PATTERN = Pattern.compile("^(.*)_([0-9]+)$");

  private static final Map<String, ArkName> NAME_CACHE = new ConcurrentHashMap<>();

  public static final ArkName NAME_NONE = from("None");

  public static ArkName from(String name) {
    return NAME_CACHE.computeIfAbsent(name, arkName -> new ArkName(arkName));
  }

  public static ArkName from(String name, int instance) {
    final String string = (instance == 0) ? name : name + "_" + (instance - 1);

    return NAME_CACHE.computeIfAbsent(string, arkName -> new ArkName(name, instance, arkName));
  }

  private final String name;

  private final int instance;

  private final String string;

  private ArkName(String string) {
    Matcher matcher = NAME_INDEX_PATTERN.matcher(string);
    if (matcher.matches()) {
      this.name = matcher.group(1);
      this.instance = Integer.parseInt(matcher.group(2)) + 1;
    } else {
      this.name = string;
      this.instance = 0;
    }
    this.string = string;
  }

  private ArkName(String name, int instance, String string) {
    this.name = name;
    this.instance = instance;
    this.string = string;
  }

  public String getName() {
    return name;
  }

  public int getInstance() {
    return instance;
  }

  @Override
  public String toString() {
    return string;
  }

  @Override
  public int hashCode() {
    return string.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof ArkName))
      return false;
    return string.equals(((ArkName) obj).string);
  }

}
