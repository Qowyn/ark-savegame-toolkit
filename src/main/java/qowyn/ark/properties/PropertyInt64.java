package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;

public class PropertyInt64 extends PropertyBase<Long> {

  public PropertyInt64(String name, String typeName, long value) {
    super(name, typeName, 0, value);
  }

  public PropertyInt64(String name, String typeName, int index, long value) {
    super(name, typeName, index, value);
  }

  public PropertyInt64(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
    value = archive.getLong();
  }

  public PropertyInt64(JsonObject o) {
    super(o);
    value = o.getJsonNumber("value").longValueExact();
  }

  @Override
  public Class<Long> getValueClass() {
    return Long.class;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    job.add("value", value);
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putLong(value);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return Long.BYTES;
  }

}
