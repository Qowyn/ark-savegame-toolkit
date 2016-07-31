package qowyn.ark.arrays;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.json.JsonArray;

import qowyn.ark.ArkArchive;

public final class ArkArrayReader {

  public static final Map<String, Function<ArkArchive, ArkArray<?>>> ARRAY_TYPE_MAP = new HashMap<>();

  public static final Map<String, Function<JsonArray, ArkArray<?>>> ARRAY_JSON_TYPE_MAP = new HashMap<>();

  public static void addArray(String name, Function<ArkArchive, ArkArray<?>> binary, Function<JsonArray, ArkArray<?>> json) {
    ARRAY_TYPE_MAP.put(name, binary);
    ARRAY_JSON_TYPE_MAP.put(name, json);
  }

  static {
    addArray("ObjectProperty", ArkArrayObjectReference::new, ArkArrayObjectReference::new);
    addArray("StructProperty", ArkArrayStruct::new, ArkArrayStruct::new);
    addArray("UInt32Property", ArkArrayInteger::new, ArkArrayInteger::new);
    addArray("IntProperty", ArkArrayInteger::new, ArkArrayInteger::new);
    addArray("StrProperty", ArkArrayString::new, ArkArrayString::new);
  }

  public static ArkArray<?> read(ArkArchive archive, String arrayType) {
    if (ARRAY_TYPE_MAP.containsKey(arrayType)) {
      return ARRAY_TYPE_MAP.get(arrayType).apply(archive);
    } else {
      System.err.println("Warning: Unknown Array Type " + arrayType + " at " + Integer.toHexString(archive.position()));
      return null;
    }
  }

  public static ArkArray<?> read(JsonArray a, String arrayType) {
    if (ARRAY_JSON_TYPE_MAP.containsKey(arrayType)) {
      return ARRAY_JSON_TYPE_MAP.get(arrayType).apply(a);
    } else {
      System.err.println("Warning: Unknown Array Type " + arrayType);
      return null;
    }
  }

  private ArkArrayReader() {}

}
