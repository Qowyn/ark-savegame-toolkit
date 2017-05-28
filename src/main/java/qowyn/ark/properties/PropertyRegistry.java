package qowyn.ark.properties;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyRegistry {

  public static final Map<ArkName, PropertyBinaryConstructor> TYPE_MAP = new HashMap<>();

  public static final Map<ArkName, PropertyJsonConstructor> TYPE_JSON_MAP = new HashMap<>();

  public static void addProperty(ArkName name, PropertyBinaryConstructor binary, PropertyJsonConstructor json) {
    TYPE_MAP.put(name, binary);
    TYPE_JSON_MAP.put(name, json);
  }

  static {
    addProperty(PropertyInt8.TYPE, PropertyInt8::new, PropertyInt8::new);
    addProperty(PropertyByte.TYPE, PropertyByte::new, PropertyByte::new);
    addProperty(PropertyInt16.TYPE, PropertyInt16::new, PropertyInt16::new);
    addProperty(PropertyUInt16.TYPE, PropertyUInt16::new, PropertyUInt16::new);
    addProperty(PropertyInt.TYPE, PropertyInt::new, PropertyInt::new);
    addProperty(PropertyUInt32.TYPE, PropertyUInt32::new, PropertyUInt32::new);
    addProperty(PropertyInt64.TYPE, PropertyInt64::new, PropertyInt64::new);
    addProperty(PropertyUInt64.TYPE, PropertyUInt64::new, PropertyUInt64::new);
    addProperty(PropertyFloat.TYPE, PropertyFloat::new, PropertyFloat::new);
    addProperty(PropertyDouble.TYPE, PropertyDouble::new, PropertyDouble::new);
    addProperty(PropertyBool.TYPE, PropertyBool::new, PropertyBool::new);
    addProperty(PropertyStr.TYPE, PropertyStr::new, PropertyStr::new);
    addProperty(PropertyName.TYPE, PropertyName::new, PropertyName::new);
    addProperty(PropertyText.TYPE, PropertyText::new, PropertyText::new);
    addProperty(PropertyObject.TYPE, PropertyObject::new, PropertyObject::new);
    addProperty(PropertyArray.TYPE, PropertyArray::new, PropertyArray::new);
    addProperty(PropertyStruct.TYPE, PropertyStruct::new, PropertyStruct::new);
  }

  public static Property<?> readProperty(ArkArchive archive) {
    ArkName name = archive.getName();

    if (name == null || name.toString().isEmpty()) {
      archive.unknownData();
      throw new UnreadablePropertyException("Property name is " + (name == null ? "null" : "empty") + ", indicating a corrupt file. Ignoring remaining properties.");
    }

    if (name == ArkName.NAME_NONE) {
      return null;
    }

    ArkName type = archive.getName();

    if (!TYPE_MAP.containsKey(type)) {
      System.err.println("Warning: Unknown property type " + type + " near " + Integer.toHexString(archive.position()) + ".");
      archive.unknownNames();
      return new PropertyUnknown(archive, name, type);
    }

    return TYPE_MAP.get(type).apply(archive, name);
  }

  public static Property<?> fromJSON(JsonObject o) {
    ArkName type = ArkName.from(o.getString("type"));

    return TYPE_JSON_MAP.getOrDefault(type, PropertyUnknown::new).apply(o);
  }

}
