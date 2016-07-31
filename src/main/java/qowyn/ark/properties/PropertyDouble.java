package qowyn.ark.properties;

import qowyn.ark.ArkArchive;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

public class PropertyDouble extends PropertyBase<Double> {

  private double value;

  public PropertyDouble(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
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
  public Double getValue() {
    return value;
  }

  @Override
  public void setValue(Double value) {
    this.value = value;
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
