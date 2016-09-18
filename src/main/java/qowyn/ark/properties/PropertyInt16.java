package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;

public class PropertyInt16 extends PropertyBase<Short> {

  public PropertyInt16(String name, String typeName, short value) {
    super(name, typeName, 0, value);
  }

  public PropertyInt16(String name, String typeName, int index, short value) {
    super(name, typeName, index, value);
  }

  public PropertyInt16(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
    value = archive.getShort();
  }

  public PropertyInt16(JsonObject o) {
    super(o);
    value = (short) o.getInt("value");
  }

  @Override
  public Class<Short> getValueClass() {
    return Short.class;
  }

  @Override
  public Short getValue() {
    return value;
  }

  @Override
  public void setValue(Short value) {
    this.value = value;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    job.add("value", value);
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putShort(value);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return Short.BYTES;
  }

}
