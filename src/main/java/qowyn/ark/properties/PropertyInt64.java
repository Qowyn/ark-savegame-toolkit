package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;

public class PropertyInt64 extends PropertyBase<Long> {

  private long value;

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
  public Long getValue() {
    return value;
  }

  @Override
  public void setValue(Long value) {
    this.value = value;
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
