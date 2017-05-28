package qowyn.ark.arrays;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.properties.UnreadablePropertyException;
import qowyn.ark.types.ArkName;

public final class ArkArrayRegistry {

  public static final Map<ArkName, ArkArrayBinaryConstructor> ARRAY_TYPE_MAP = new HashMap<>();

  public static final Map<ArkName, ArkArrayJsonConstructor> ARRAY_JSON_TYPE_MAP = new HashMap<>();

  public static void addArray(ArkName name, ArkArrayBinaryConstructor binary, ArkArrayJsonConstructor json) {
    ARRAY_TYPE_MAP.put(name, binary);
    ARRAY_JSON_TYPE_MAP.put(name, json);
  }

  static {
    addArray(ArkArrayInt8.TYPE_SIGNED, ArkArrayInt8::new, ArkArrayInt8::new);
    addArray(ArkArrayInt8.TYPE_UNSIGNED, ArkArrayByteHandler::create, ArkArrayByteHandler::create);
    addArray(ArkArrayInt16.TYPE_SIGNED, ArkArrayInt16::new, ArkArrayInt16::new);
    addArray(ArkArrayInt16.TYPE_UNSIGNED, ArkArrayInt16::new, ArkArrayInt16::new);
    addArray(ArkArrayInteger.TYPE_SIGNED, ArkArrayInteger::new, ArkArrayInteger::new);
    addArray(ArkArrayInteger.TYPE_UNSIGNED, ArkArrayInteger::new, ArkArrayInteger::new);
    addArray(ArkArrayLong.TYPE_SIGNED, ArkArrayLong::new, ArkArrayLong::new);
    addArray(ArkArrayLong.TYPE_UNSIGNED, ArkArrayLong::new, ArkArrayLong::new);
    addArray(ArkArrayFloat.TYPE, ArkArrayFloat::new, ArkArrayFloat::new);
    addArray(ArkArrayDouble.TYPE, ArkArrayDouble::new, ArkArrayDouble::new);
    addArray(ArkArrayBool.TYPE, ArkArrayBool::new, ArkArrayBool::new);
    addArray(ArkArrayString.TYPE, ArkArrayString::new, ArkArrayString::new);
    addArray(ArkArrayName.TYPE, ArkArrayName::new, ArkArrayName::new);
    addArray(ArkArrayObjectReference.TYPE, ArkArrayObjectReference::new, ArkArrayObjectReference::new);
    addArray(ArkArrayStruct.TYPE, ArkArrayStruct::new, ArkArrayStruct::new);
  }

  public static ArkArray<?> read(ArkArchive archive, PropertyArray property) {
    if (ARRAY_TYPE_MAP.containsKey(property.getArrayType())) {
      return ARRAY_TYPE_MAP.get(property.getArrayType()).apply(archive, property);
    } else {
      throw new UnreadablePropertyException("Unknown Array Type " + property.getArrayType() + " at " + Integer.toHexString(archive.position()));
    }
  }

  public static ArkArray<?> read(JsonArray a, PropertyArray property) {
    if (ARRAY_JSON_TYPE_MAP.containsKey(property.getArrayType())) {
      return ARRAY_JSON_TYPE_MAP.get(property.getArrayType()).apply(a, property);
    } else {
      throw new UnreadablePropertyException("Unknown Array Type " + property.getArrayType());
    }
  }

  private ArkArrayRegistry() {}

}
