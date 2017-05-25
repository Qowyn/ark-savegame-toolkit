package qowyn.ark.arrays;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.UnreadablePropertyException;
import qowyn.ark.types.ArkName;

public final class ArkArrayRegistry {

  public static final Map<ArkName, ArkArrayBinaryConstructor> ARRAY_TYPE_MAP = new HashMap<>();

  public static final Map<ArkName, ArkArrayJsonConstructor> ARRAY_JSON_TYPE_MAP = new HashMap<>();

  public static void addArray(String name, ArkArrayBinaryConstructor binary, ArkArrayJsonConstructor json) {
    ARRAY_TYPE_MAP.put(ArkName.from(name), binary);
    ARRAY_JSON_TYPE_MAP.put(ArkName.from(name), json);
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

  public static ArkArray<?> read(ArkArchive archive, ArkName arrayType, int size, ArkName propertyName) {
    if (ARRAY_TYPE_MAP.containsKey(arrayType)) {
      return ARRAY_TYPE_MAP.get(arrayType).apply(archive, size, propertyName);
    } else {
      throw new UnreadablePropertyException("Unknown Array Type " + arrayType + " at " + Integer.toHexString(archive.position()));
    }
  }

  public static ArkArray<?> read(JsonValue v, ArkName arrayType, int size, ArkName propertyName) {
    if (ARRAY_JSON_TYPE_MAP.containsKey(arrayType)) {
      return ARRAY_JSON_TYPE_MAP.get(arrayType).apply(v, size, propertyName);
    } else {
      throw new UnreadablePropertyException("Unknown Array Type " + arrayType);
    }
  }

  private ArkArrayRegistry() {}

}
