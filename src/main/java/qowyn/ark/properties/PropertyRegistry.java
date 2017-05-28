package qowyn.ark.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.json.JsonObject;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyRegistry {

  public static final Map<ArkName, BiFunction<ArkArchive, PropertyArgs, Property<?>>> TYPE_MAP = new HashMap<>();

  public static final Map<ArkName, Function<JsonObject, Property<?>>> TYPE_JSON_MAP = new HashMap<>();

  public static void addProperty(ArkName name, BiFunction<ArkArchive, PropertyArgs, Property<?>> binary, Function<JsonObject, Property<?>> json) {
    TYPE_MAP.put(name, binary);
    TYPE_JSON_MAP.put(name, json);
  }

  static {
    addProperty(PropertyInt8.TYPE, PropertyInt8::new, PropertyInt8::new);
    addProperty(PropertyByte.TYPE, PropertyByte::new, PropertyByte::new);
    addProperty(PropertyInt16.TYPE_SIGNED, PropertyInt16::new, PropertyInt16::new);
    addProperty(PropertyInt16.TYPE_UNSIGNED, PropertyInt16::new, PropertyInt16::new);
    addProperty(PropertyInt32.TYPE_SIGNED, PropertyInt32::new, PropertyInt32::new);
    addProperty(PropertyInt32.TYPE_UNSIGNED, PropertyInt32::new, PropertyInt32::new);
    addProperty(PropertyInt64.TYPE_SIGNED, PropertyInt64::new, PropertyInt64::new);
    addProperty(PropertyInt64.TYPE_UNSIGNED, PropertyInt64::new, PropertyInt64::new);
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

    PropertyArgs args = new PropertyArgs(name, type);

    if (!TYPE_MAP.containsKey(type)) {
      System.err.println("Warning: Unknown property type " + type + " near " + Integer.toHexString(archive.position()) + ".");
      archive.unknownNames();
    }

    return TYPE_MAP.getOrDefault(type, PropertyUnknown::new).apply(archive, args);
  }

  public static Property<?> fromJSON(JsonObject o) {
    ArkName type = ArkName.from(o.getString("type"));

    return TYPE_JSON_MAP.getOrDefault(type, PropertyUnknown::new).apply(o);
  }

}
