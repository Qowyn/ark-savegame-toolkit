package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;

public class PropertyInt32 extends PropertyBase<Integer> {

  public PropertyInt32(String name, String typeName, int value) {
    super(name, typeName, 0, value);
  }

  public PropertyInt32(String name, String typeName, int index, int value) {
    super(name, typeName, index, value);
  }

  public PropertyInt32(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
    value = archive.getInt();
  }

  public PropertyInt32(JsonObject o) {
    super(o);
    value = o.getInt("value");
  }

  @Override
  public Class<Integer> getValueClass() {
    return Integer.class;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    job.add("value", value);
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putInt(value);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return Integer.BYTES;
  }

}
