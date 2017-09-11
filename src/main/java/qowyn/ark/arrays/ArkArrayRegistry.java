package qowyn.ark.arrays;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

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
    addArray(ArkArrayInt8.TYPE, ArkArrayInt8::new, ArkArrayInt8::new);
    addArray(ArkArrayByteHandler.TYPE, ArkArrayByteHandler::create, ArkArrayByteHandler::create);
    addArray(ArkArrayInt16.TYPE, ArkArrayInt16::new, ArkArrayInt16::new);
    addArray(ArkArrayUInt16.TYPE, ArkArrayUInt16::new, ArkArrayUInt16::new);
    addArray(ArkArrayInt.TYPE, ArkArrayInt::new, ArkArrayInt::new);
    addArray(ArkArrayUInt32.TYPE, ArkArrayUInt32::new, ArkArrayUInt32::new);
    addArray(ArkArrayInt64.TYPE, ArkArrayInt64::new, ArkArrayInt64::new);
    addArray(ArkArrayUInt64.TYPE, ArkArrayUInt64::new, ArkArrayUInt64::new);
    addArray(ArkArrayFloat.TYPE, ArkArrayFloat::new, ArkArrayFloat::new);
    addArray(ArkArrayDouble.TYPE, ArkArrayDouble::new, ArkArrayDouble::new);
    addArray(ArkArrayBool.TYPE, ArkArrayBool::new, ArkArrayBool::new);
    addArray(ArkArrayString.TYPE, ArkArrayString::new, ArkArrayString::new);
    addArray(ArkArrayName.TYPE, ArkArrayName::new, ArkArrayName::new);
    addArray(ArkArrayObjectReference.TYPE, ArkArrayObjectReference::new, ArkArrayObjectReference::new);
    addArray(ArkArrayStruct.TYPE, ArkArrayStruct::new, ArkArrayStruct::new);
  }

  public static ArkArray<?> readBinary(ArkArchive archive, ArkName arrayType, PropertyArray property) {
    if (ARRAY_TYPE_MAP.containsKey(arrayType)) {
      return ARRAY_TYPE_MAP.get(arrayType).apply(archive, property);
    } else {
      throw new UnreadablePropertyException("Unknown Array Type " + arrayType + " at " + Integer.toHexString(archive.position()));
    }
  }

  public static ArkArray<?> readJson(JsonNode node, ArkName arrayType, PropertyArray property) {
    if (ARRAY_JSON_TYPE_MAP.containsKey(arrayType)) {
      return ARRAY_JSON_TYPE_MAP.get(arrayType).apply(node, property);
    } else {
      throw new UnreadablePropertyException("Unknown Array Type " + arrayType);
    }
  }

  private ArkArrayRegistry() {}

}
