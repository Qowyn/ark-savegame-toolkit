package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;

public class PropertyInt8 extends PropertyBase<Byte> {

  public PropertyInt8(String name, String typeName, byte value) {
    super(name, typeName, 0, value);
  }

  public PropertyInt8(String name, String typeName, int index, byte value) {
    super(name, typeName, index, value);
  }

  public PropertyInt8(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
    value = archive.getByte();
  }

  public PropertyInt8(JsonObject o) {
    super(o);
    value = (byte) o.getInt("value");
  }

  @Override
  public Class<Byte> getValueClass() {
    return Byte.class;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    job.add("value", value);
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putByte(value);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return Byte.BYTES;
  }

}
