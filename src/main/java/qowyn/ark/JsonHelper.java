package qowyn.ark;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

public class JsonHelper {

  public static float getFloat(JsonObject object, String name) {
    return getFloat(object, name, 0.0f);
  }

  public static float getFloat(JsonObject object, String name, float defaultValue) {
    JsonValue value = object.get(name);

    if (value != null) {
      if (value.getValueType() == ValueType.NUMBER) {
        return ((JsonNumber) value).bigDecimalValue().floatValue();
      } else if (value.getValueType() == ValueType.STRING) {
        return Float.parseFloat(((JsonString) value).getString());
      }
    }

    return defaultValue;
  }

  public static void addFloat(JsonObjectBuilder builder, String name, float value) {
    addFloat(builder, name, value, 0.0f);
  }

  public static void addFloat(JsonObjectBuilder builder, String name, float value, float defaultValue) {
    if (value != defaultValue) {
      if (Float.isFinite(value)) {
        builder.add(name, value);
      } else { // NaN, +Infinity, -Infinity
        builder.add(name, Float.toString(value));
      }
    }
  }

  public static void addInt(JsonObjectBuilder builder, String name, int value) {
    addInt(builder, name, value, 0);
  }

  public static void addInt(JsonObjectBuilder builder, String name, int value, int defaultValue) {
    if (value != defaultValue) {
      builder.add(name, value);
    }
  }

  public static void addString(JsonObjectBuilder builder, String name, String value) {
    addString(builder, name, value, "");
  }

  public static void addString(JsonObjectBuilder builder, String name, String value, String defaultValue) {
    if (!defaultValue.equals(value)) {
      builder.add(name, value);
    }
  }

}
