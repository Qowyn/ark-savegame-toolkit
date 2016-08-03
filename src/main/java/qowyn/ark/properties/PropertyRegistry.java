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

  public static void addProperty(String name, BiFunction<ArkArchive, PropertyArgs, Property<?>> binary, Function<JsonObject, Property<?>> json) {
    TYPE_MAP.put(new ArkName(name), binary);
    TYPE_JSON_MAP.put(new ArkName(name), json);
  }

  static {
    addProperty("IntProperty", PropertyInt32::new, PropertyInt32::new);
    addProperty("UInt32Property", PropertyInt32::new, PropertyInt32::new);
    addProperty("BoolProperty", PropertyBool::new, PropertyBool::new);
    addProperty("ByteProperty", PropertyByte::new, PropertyByte::new);
    addProperty("ObjectProperty", PropertyObject::new, PropertyObject::new);
    addProperty("FloatProperty", PropertyFloat::new, PropertyFloat::new);
    addProperty("StrProperty", PropertyStr::new, PropertyStr::new);
    addProperty("DoubleProperty", PropertyDouble::new, PropertyDouble::new);
    addProperty("ArrayProperty", PropertyArray::new, PropertyArray::new);
    addProperty("StructProperty", PropertyStruct::new, PropertyStruct::new);
    addProperty("Int8Property", PropertyInt8::new, PropertyInt8::new);
    addProperty("UInt16Property", PropertyInt16::new, PropertyInt16::new);
    addProperty("Int16Property", PropertyInt16::new, PropertyInt16::new);
    addProperty("UInt64Property", PropertyInt64::new, PropertyInt64::new);
    addProperty("NameProperty", PropertyName::new, PropertyName::new);
  }

  public static Property<?> readProperty(ArkArchive archive) {
    ArkName name = archive.getName();

    if (name == null || name.equals(Property.EMPTY_NAME)) {
      System.err.println("Warning: Property name is " + (name == null ? "null" : "empty") + ". Ignoring remaining properties.");
      throw new UnreadablePropertyException();
    }

    if (name.equals(Property.NONE_NAME)) {
      return null;
    }

    ArkName type = archive.getName();

    if (!TYPE_MAP.containsKey(type)) {
      System.err.println("Warning: Unknown property type " + type + " near " + Integer.toHexString(archive.position()) + ". Ignoring remaining properties.");
      throw new UnreadablePropertyException();
    }

    PropertyArgs args = new PropertyArgs(name, type);

    return TYPE_MAP.get(type).apply(archive, args);
  }

  public static Property<?> fromJSON(JsonObject o) {
    ArkName type = new ArkName(o.getString("type"));

    return TYPE_JSON_MAP.get(type).apply(o);
  }

}
