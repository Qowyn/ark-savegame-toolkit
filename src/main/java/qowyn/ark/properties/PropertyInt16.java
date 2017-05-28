package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyInt16 extends PropertyBase<Short> {

  public static final ArkName TYPE = ArkName.constantPlain("Int16Property");

  public PropertyInt16(String name, short value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyInt16(String name, int index, short value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyInt16(ArkArchive archive, ArkName name) {
    super(archive, name);
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
  public ArkName getType() {
    return TYPE;
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
