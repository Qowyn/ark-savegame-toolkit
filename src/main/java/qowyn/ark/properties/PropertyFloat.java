package qowyn.ark.properties;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyFloat extends PropertyBase<Float> {

  public static final ArkName TYPE = ArkName.constantPlain("FloatProperty");

  public PropertyFloat(String name, float value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyFloat(String name, int index, float value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyFloat(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getFloat();
  }

  public PropertyFloat(JsonObject o) {
    super(o);
    JsonValue v = o.get("value");
    if (v.getValueType() == ValueType.STRING) {
      JsonString s = (JsonString) v;
      value = Float.valueOf(s.getString());
    } else {
      JsonNumber n = (JsonNumber) v;
      value = n.bigDecimalValue().floatValue();
    }
  }

  @Override
  public Class<Float> getValueClass() {
    return Float.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    if (Float.isFinite(value)) {
      job.add("value", value);
    } else {
      job.add("value", Float.toString(value));
    }
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putFloat(value);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return Float.BYTES;
  }

}
