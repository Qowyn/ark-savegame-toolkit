package qowyn.ark.arrays;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import javax.json.JsonArray;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public final class ArkArrayRegistry {

  public static final Map<ArkName, BiFunction<ArkArchive, ArkName, ArkArray<?>>> ARRAY_TYPE_MAP = new HashMap<>();

  public static final Map<ArkName, BiFunction<JsonArray, ArkName, ArkArray<?>>> ARRAY_JSON_TYPE_MAP = new HashMap<>();

  public static void addArray(String name, BiFunction<ArkArchive, ArkName, ArkArray<?>> binary, BiFunction<JsonArray, ArkName, ArkArray<?>> json) {
    ARRAY_TYPE_MAP.put(new ArkName(name), binary);
    ARRAY_JSON_TYPE_MAP.put(new ArkName(name), json);
  }

  static {
    addArray("ObjectProperty", ArkArrayObjectReference::new, ArkArrayObjectReference::new);
    addArray("StructProperty", ArkArrayStruct::new, ArkArrayStruct::new);
    addArray("UInt32Property", ArkArrayInteger::new, ArkArrayInteger::new);
    addArray("IntProperty", ArkArrayInteger::new, ArkArrayInteger::new);
    addArray("StrProperty", ArkArrayString::new, ArkArrayString::new);
  }

  public static ArkArray<?> read(ArkArchive archive, ArkName arrayType, ArkName propertyName) {
    if (ARRAY_TYPE_MAP.containsKey(arrayType)) {
      return ARRAY_TYPE_MAP.get(arrayType).apply(archive, propertyName);
    } else {
      System.err.println("Warning: Unknown Array Type " + arrayType + " at " + Integer.toHexString(archive.position()));
      return null;
    }
  }

  public static ArkArray<?> read(JsonArray a, ArkName arrayType, ArkName propertyName) {
    if (ARRAY_JSON_TYPE_MAP.containsKey(arrayType)) {
      return ARRAY_JSON_TYPE_MAP.get(arrayType).apply(a, propertyName);
    } else {
      System.err.println("Warning: Unknown Array Type " + arrayType);
      return null;
    }
  }

  private ArkArrayRegistry() {}

}
