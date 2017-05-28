package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyInt16 extends PropertyBase<Short> {

  public static final ArkName TYPE_SIGNED = ArkName.constantPlain("Int16Property");

  public static final ArkName TYPE_UNSIGNED = ArkName.constantPlain("UInt16Property");

  public PropertyInt16(String name, ArkName typeName, short value) {
    super(ArkName.from(name), typeName, 0, value);
  }

  public PropertyInt16(String name, ArkName typeName, int index, short value) {
    super(ArkName.from(name), typeName, index, value);
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
