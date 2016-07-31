package qowyn.ark;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class JsonHelper {

  public static float getFloat(JsonObject object, String name) {
    return getFloat(object, name, 0.0f);
  }

  public static float getFloat(JsonObject object, String name, float defaultValue) {
    JsonNumber n = object.getJsonNumber(name);

    if (n != null) {
      return n.bigDecimalValue().floatValue();
    }

    return defaultValue;
  }

  public static void addFloat(JsonObjectBuilder builder, String name, float value) {
    addFloat(builder, name, value, 0.0f);
  }

  public static void addFloat(JsonObjectBuilder builder, String name, float value, float defaultValue) {
    if (value != defaultValue) {
      builder.add(name, value);
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
