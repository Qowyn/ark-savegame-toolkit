package qowyn.ark.arrays;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import javax.json.JsonArray;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.properties.PropertyBool;
import qowyn.ark.properties.PropertyByte;
import qowyn.ark.properties.PropertyDouble;
import qowyn.ark.properties.PropertyFloat;
import qowyn.ark.properties.PropertyInt16;
import qowyn.ark.properties.PropertyInt32;
import qowyn.ark.properties.PropertyInt64;
import qowyn.ark.properties.PropertyInt8;
import qowyn.ark.properties.PropertyName;
import qowyn.ark.properties.PropertyObject;
import qowyn.ark.properties.PropertyStr;
import qowyn.ark.properties.PropertyStruct;
import qowyn.ark.types.ArkName;

public final class ArkArrayRegistry {

  public static final Map<ArkName, BiFunction<ArkArchive, Integer, ArkArray<?>>> ARRAY_TYPE_MAP = new HashMap<>();

  public static final Map<ArkName, BiFunction<JsonArray, Integer, ArkArray<?>>> ARRAY_JSON_TYPE_MAP = new HashMap<>();

  public static void addArray(String name, BiFunction<ArkArchive, Integer, ArkArray<?>> binary, BiFunction<JsonArray, Integer, ArkArray<?>> json) {
    ARRAY_TYPE_MAP.put(new ArkName(name), binary);
    ARRAY_JSON_TYPE_MAP.put(new ArkName(name), json);
  }

  static {
    addArray("ObjectProperty", ArkArrayObjectReference::new, ArkArrayObjectReference::new);
    addArray("StructProperty", ArkArrayStruct::new, ArkArrayStruct::new);
    addArray("UInt32Property", ArkArrayInteger::new, ArkArrayInteger::new);
    addArray("IntProperty", ArkArrayInteger::new, ArkArrayInteger::new);
    addArray("UInt16Property", ArkArrayInt16::new, ArkArrayInt16::new);
    addArray("Int16Property", ArkArrayInt16::new, ArkArrayInt16::new);
    addArray("ByteProperty", ArkArrayByte::new, ArkArrayByte::new);
    addArray("Int8Property", ArkArrayInt8::new, ArkArrayInt8::new);
    addArray("StrProperty", ArkArrayString::new, ArkArrayString::new);
    addArray("UInt64Property", ArkArrayLong::new, ArkArrayLong::new);
    addArray("BoolProperty", ArkArrayBool::new, ArkArrayBool::new);
    addArray("FloatProperty", ArkArrayFloat::new, ArkArrayFloat::new);
    addArray("DoubleProperty", ArkArrayDouble::new, ArkArrayDouble::new);
    addArray("NameProperty", ArkArrayName::new, ArkArrayName::new);
  }

  public static ArkArray<?> read(ArkArchive archive, ArkName arrayType, int size) {
    if (ARRAY_TYPE_MAP.containsKey(arrayType)) {
      return ARRAY_TYPE_MAP.get(arrayType).apply(archive, size);
    } else {
      System.err.println("Warning: Unknown Array Type " + arrayType + " at " + Integer.toHexString(archive.position()));
      return null;
    }
  }

  public static ArkArray<?> read(JsonArray a, ArkName arrayType, int size) {
    if (ARRAY_JSON_TYPE_MAP.containsKey(arrayType)) {
      return ARRAY_JSON_TYPE_MAP.get(arrayType).apply(a, size);
    } else {
      System.err.println("Warning: Unknown Array Type " + arrayType);
      return null;
    }
  }

  private ArkArrayRegistry() {}

}
