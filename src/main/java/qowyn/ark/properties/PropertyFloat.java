package qowyn.ark.properties;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import qowyn.ark.ArkArchive;

public class PropertyFloat extends PropertyBase<Float> {

  public PropertyFloat(String name, String typeName, float value) {
    super(name, typeName, 0, value);
  }

  public PropertyFloat(String name, String typeName, int index, float value) {
    super(name, typeName, index, value);
  }

  public PropertyFloat(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
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
  public Float getValue() {
    return value;
  }

  @Override
  public void setValue(Float value) {
    this.value = value;
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
