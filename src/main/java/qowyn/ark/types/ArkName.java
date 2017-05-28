package qowyn.ark.types;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ArkName implements Serializable, Comparable<ArkName>, CharSequence {

  private static final long serialVersionUID = 1L;

  private static final Object LOCK = new Object();

  private static final Pattern NAME_INDEX_PATTERN = Pattern.compile("^(.*)_([0-9]+)$");

  private static volatile ConcurrentHashMap<String, ArkName> NAME_CACHE = new ConcurrentHashMap<>();

  private static final ConcurrentHashMap<String, ArkName> CONSTANT_NAME_CACHE = new ConcurrentHashMap<>();

  public static final ArkName NAME_NONE = constantPlain("None");

  /**
   * Creates or retrieves an ArkName
   * @param name
   * @return
   */
  public static ArkName from(String name) {
    return NAME_CACHE.computeIfAbsent(name, arkName -> new ArkName(arkName));
  }

  /**
   * Creates or retrieves an ArkName
   * @param name
   * @return
   */
  public static ArkName from(String name, int instance) {
    final String string = (instance == 0) ? name : name + "_" + (instance - 1);

    return NAME_CACHE.computeIfAbsent(string, arkName -> new ArkName(name, instance, arkName));
  }

  /**
   * Creates or retrieves an ArkName with instance 0
   * @param name
   * @return
   */
  public static ArkName fromPlain(String name) {
    return NAME_CACHE.computeIfAbsent(name, arkName -> new ArkName(name, 0, arkName));
  }

  /**
   * Creates or retrieves an permanent ArkName
   * @param name
   * @return
   */
  public static ArkName constant(String name) {
    synchronized (LOCK) {
      ArkName result = NAME_CACHE.computeIfAbsent(name, arkName -> new ArkName(arkName));
      CONSTANT_NAME_CACHE.put(name, result);
      return result;
    }
  }

  /**
   * Creates or retrieves an permanent ArkName
   * @param name
   * @return
   */
  public static ArkName constant(String name, int instance) {
    synchronized (LOCK) {
      final String string = (instance == 0) ? name : name + "_" + (instance - 1);

      ArkName result = NAME_CACHE.computeIfAbsent(string, arkName -> new ArkName(name, instance, arkName));
      CONSTANT_NAME_CACHE.put(string, result);
      return result;
    }
  }

  /**
   * Creates or retrieves an permanent ArkName with instance 0
   * @param name
   * @return
   */
  public static ArkName constantPlain(String name) {
    synchronized (LOCK) {
      ArkName result = NAME_CACHE.computeIfAbsent(name, arkName -> new ArkName(name, 0, arkName));
      CONSTANT_NAME_CACHE.put(name, result);
      return result;
    }
  }

  public static void clearCache() {
    synchronized (LOCK) {
      NAME_CACHE = new ConcurrentHashMap<>(CONSTANT_NAME_CACHE);
    }
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

  @Override
  public int compareTo(ArkName o) {
    return string.compareTo(o.string);
  }

  @Override
  public int length() {
    return string.length();
  }

  @Override
  public char charAt(int index) {
    return string.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return string.substring(start, end);
  }

}
