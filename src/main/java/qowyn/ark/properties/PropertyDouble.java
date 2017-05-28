package qowyn.ark.properties;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

public class PropertyDouble extends PropertyBase<Double> {

  public static final ArkName TYPE = ArkName.constantPlain("DoubleProperty");

  public PropertyDouble(String name, double value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyDouble(String name, int index, double value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyDouble(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getDouble();
  }

  public PropertyDouble(JsonObject o) {
    super(o);
    JsonValue v = o.get("value");
    if (v.getValueType() == ValueType.STRING) {
      JsonString s = (JsonString) v;
      value = Double.valueOf(s.getString());
    } else {
      JsonNumber n = (JsonNumber) v;
      value = n.bigDecimalValue().doubleValue();
    }
  }

  @Override
  public Class<Double> getValueClass() {
    return Double.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    if (Double.isFinite(value)) {
      job.add("value", value);
    } else {
      job.add("value", Double.toString(value));
    }
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putDouble(value);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return Double.BYTES;
  }

}
