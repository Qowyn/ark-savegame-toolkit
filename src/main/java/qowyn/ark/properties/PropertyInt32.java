package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyInt32 extends PropertyBase<Integer> {

  public static final ArkName TYPE_SIGNED = ArkName.constantPlain("IntProperty");

  public static final ArkName TYPE_UNSIGNED = ArkName.constantPlain("UInt32Property");

  public PropertyInt32(String name, ArkName typeName, int value) {
    super(ArkName.from(name), typeName, 0, value);
  }

  public PropertyInt32(String name, ArkName typeName, int index, int value) {
    super(ArkName.from(name), typeName, index, value);
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
